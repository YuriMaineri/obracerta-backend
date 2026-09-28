package br.com.obracerta.company;

import br.com.obracerta.company.internal.BankAccountRepository;
import br.com.obracerta.company.internal.ClauseRepository;
import br.com.obracerta.company.internal.CompanyRepository;
import br.com.obracerta.shared.BrazilianDocuments;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/** API do módulo empresa. O sistema atende uma única empresa, criada pela migration V3. */
@Service
@Transactional(readOnly = true)
public class CompanyService {

    static final long MAX_LOGO_BYTES = 1024 * 1024;

    private final CompanyRepository companies;
    private final BankAccountRepository bankAccounts;
    private final ClauseRepository clauses;

    CompanyService(CompanyRepository companies, BankAccountRepository bankAccounts, ClauseRepository clauses) {
        this.companies = companies;
        this.bankAccounts = bankAccounts;
        this.clauses = clauses;
    }

    public Company getCompany() {
        return companies.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NoSuchElementException("Empresa nao cadastrada"));
    }

    @Transactional
    public Company updateCompany(CompanyRequest request) {
        Company company = getCompany();
        company.update(request,
                BrazilianDocuments.normalizeCnpj(request.taxId()),
                BrazilianDocuments.normalizePhone(request.phone()),
                BrazilianDocuments.normalizePostalCode(request.postalCode()));
        return company;
    }

    @Transactional
    public void replaceLogo(byte[] content) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Arquivo do logotipo vazio");
        }
        if (content.length > MAX_LOGO_BYTES) {
            throw new IllegalArgumentException("Logotipo com no maximo 1 MB");
        }
        String detected = detectImageType(content);
        if (detected == null) {
            throw new IllegalArgumentException("Logotipo deve ser PNG ou JPEG");
        }
        getCompany().replaceLogo(content, detected);
    }

    @Transactional
    public void removeLogo() {
        getCompany().removeLogo();
    }

    public List<BankAccount> listBankAccounts() {
        return bankAccounts.findByCompanyIdAndActiveTrueOrderByDefaultAccountDescBankAsc(getCompany().getId());
    }

    public Optional<BankAccount> findDefaultBankAccount() {
        return listBankAccounts().stream().filter(BankAccount::isDefaultAccount).findFirst();
    }

    public BankAccount findBankAccount(Long id) {
        return bankAccounts.findByIdAndCompanyIdAndActiveTrue(id, getCompany().getId())
                .orElseThrow(() -> new NoSuchElementException("Conta bancaria nao encontrada: " + id));
    }

    @Transactional
    public BankAccount createBankAccount(BankAccountRequest request) {
        Long companyId = getCompany().getId();
        BankAccount account = new BankAccount(companyId);
        account.update(request);
        Long id = bankAccounts.save(account).getId();
        boolean firstAccount = listBankAccounts().size() == 1;
        if (request.defaultAccount() || firstAccount) {
            makeDefault(companyId, id);
        }
        return findBankAccount(id);
    }

    @Transactional
    public BankAccount updateBankAccount(Long id, BankAccountRequest request) {
        BankAccount account = findBankAccount(id);
        account.update(request);
        if (request.defaultAccount()) {
            makeDefault(account.getCompanyId(), id);
        } else {
            account.unmarkDefault();
        }
        return findBankAccount(id);
    }

    @Transactional
    public void deactivateBankAccount(Long id) {
        findBankAccount(id).deactivate();
    }

    public List<Clause> listClauses() {
        return clauses.findByCompanyIdAndActiveTrueOrderByTypeAscSortOrderAscTitleAsc(getCompany().getId());
    }

    public Clause findClause(Long id) {
        return clauses.findByIdAndCompanyIdAndActiveTrue(id, getCompany().getId())
                .orElseThrow(() -> new NoSuchElementException("Clausula nao encontrada: " + id));
    }

    @Transactional
    public Clause createClause(ClauseRequest request) {
        Clause clause = new Clause(getCompany().getId());
        clause.update(request);
        return clauses.save(clause);
    }

    @Transactional
    public Clause updateClause(Long id, ClauseRequest request) {
        Clause clause = findClause(id);
        clause.update(request);
        return clause;
    }

    @Transactional
    public void deactivateClause(Long id) {
        findClause(id).deactivate();
    }

    /** A consulta em lote limpa o contexto do JPA, por isso a conta é recarregada depois. */
    private void makeDefault(Long companyId, Long accountId) {
        bankAccounts.clearDefaultExcept(companyId, accountId);
        bankAccounts.findById(accountId).orElseThrow().markAsDefault();
    }

    static String detectImageType(byte[] content) {
        if (content.length >= 8 && (content[0] & 0xFF) == 0x89 && content[1] == 'P' && content[2] == 'N' && content[3] == 'G') {
            return "image/png";
        }
        if (content.length >= 3 && (content[0] & 0xFF) == 0xFF && (content[1] & 0xFF) == 0xD8 && (content[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        return null;
    }
}
