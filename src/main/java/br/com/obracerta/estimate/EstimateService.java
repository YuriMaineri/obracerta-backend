package br.com.obracerta.estimate;

import br.com.obracerta.company.BankAccount;
import br.com.obracerta.company.Clause;
import br.com.obracerta.company.CompanyService;
import br.com.obracerta.customers.Customer;
import br.com.obracerta.customers.CustomerService;
import br.com.obracerta.estimate.internal.EstimateRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** API do módulo de orçamentos. Toda leitura sai já montada, porque as coleções são lazy. */
@Service
@Transactional(readOnly = true)
public class EstimateService {

    private final EstimateRepository repository;
    private final CustomerService customers;
    private final CompanyService company;

    EstimateService(EstimateRepository repository, CustomerService customers, CompanyService company) {
        this.repository = repository;
        this.customers = customers;
        this.company = company;
    }

    public Page<EstimateSummary> search(String query, EstimateStatus status, Long customerId, Pageable pageable) {
        String term = query == null ? "" : query.trim().toLowerCase();
        Specification<Estimate> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (customerId != null) {
                predicates.add(cb.equal(root.get("customerId"), customerId));
            }
            if (!term.isEmpty()) {
                String like = "%" + term + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.<String>get("number")), like),
                        cb.like(cb.lower(cb.coalesce(root.<String>get("title"), "")), like)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        Page<Estimate> page = repository.findAll(spec, pageable);
        Set<Long> customerIds = page.stream().map(Estimate::getCustomerId).collect(Collectors.toSet());
        Map<Long, Customer> names = customers.findAllByIds(customerIds);
        return page.map(e -> toSummary(e, names.get(e.getCustomerId())));
    }

    public EstimateDetails findById(Long id) {
        return toDetails(load(id));
    }

    @Transactional
    public EstimateDetails create(EstimateRequest request) {
        Estimate estimate = new Estimate(nextNumber(LocalDate.now()), request.customerId());
        Set<Long> clauseIds = request.clauseIds() == null ? defaultClauseIds() : request.clauseIds();
        Long bankAccountId = request.bankAccountId() == null
                ? company.findDefaultBankAccount().map(BankAccount::getId).orElse(null)
                : request.bankAccountId();
        apply(estimate, request, bankAccountId, clauseIds);
        return toDetails(repository.save(estimate));
    }

    @Transactional
    public EstimateDetails update(Long id, EstimateRequest request) {
        Estimate estimate = load(id);
        apply(estimate, request, request.bankAccountId(),
                request.clauseIds() == null ? Set.of() : request.clauseIds());
        return toDetails(estimate);
    }

    /** Substitui o hábito atual de copiar o arquivo do orçamento anterior. */
    @Transactional
    public EstimateDetails duplicate(Long id) {
        Estimate source = load(id);
        EstimateDetails d = toDetails(source);
        EstimateRequest copy = new EstimateRequest(d.customer().id(), d.title(), LocalDate.now(), d.validityDays(),
                d.minLeadDays(), d.maxLeadDays(), d.materialSupply(), d.estimatedMaterialCost(), d.paymentTerms(),
                d.bankAccountId(), d.clauseIds(),
                d.services().stream().map(s -> new EstimateRequest.ServiceLine(s.description(), s.internalNote())).toList(),
                d.materials().stream().map(m -> new EstimateRequest.MaterialLine(m.description(), m.quantity(), m.totalPrice())).toList(),
                d.priceLines().stream().map(p -> new EstimateRequest.PriceLine(p.description(), p.amount())).toList(),
                d.exclusions(), d.internalNotes());
        Set<Long> activeClauses = company.listClauses().stream().map(Clause::getId).collect(Collectors.toSet());
        Set<Long> clauseIds = copy.clauseIds().stream().filter(activeClauses::contains).collect(Collectors.toSet());
        Long bankAccountId = company.listBankAccounts().stream().anyMatch(a -> a.getId().equals(copy.bankAccountId()))
                ? copy.bankAccountId()
                : company.findDefaultBankAccount().map(BankAccount::getId).orElse(null);
        Estimate estimate = new Estimate(nextNumber(LocalDate.now()), copy.customerId());
        apply(estimate, copy, bankAccountId, clauseIds);
        return toDetails(repository.save(estimate));
    }

    @Transactional
    public EstimateDetails changeStatus(Long id, EstimateStatus status) {
        Estimate estimate = load(id);
        estimate.changeStatus(status);
        return toDetails(estimate);
    }

    @Transactional
    public void delete(Long id) {
        Estimate estimate = load(id);
        if (estimate.getStatus() != EstimateStatus.DRAFT) {
            throw new IllegalArgumentException("So orcamentos em rascunho podem ser excluidos; marque como recusado");
        }
        repository.delete(estimate);
    }

    private void apply(Estimate estimate, EstimateRequest request, Long bankAccountId, Set<Long> clauseIds) {
        customers.findById(request.customerId());
        if (bankAccountId != null) {
            company.findBankAccount(bankAccountId);
        }
        validateClauses(clauseIds);
        if (request.minLeadDays() != null && request.maxLeadDays() != null
                && request.minLeadDays() > request.maxLeadDays()) {
            throw new IllegalArgumentException("Prazo minimo maior que o prazo maximo");
        }
        estimate.updateHeader(request, bankAccountId);
        estimate.replaceServices(orEmpty(request.services()));
        estimate.replaceMaterials(orEmpty(request.materials()));
        estimate.replacePriceLines(orEmpty(request.priceLines()));
        estimate.replaceExclusions(orEmpty(request.exclusions()));
        estimate.replaceInternalNotes(orEmpty(request.internalNotes()));
        estimate.replaceClauses(clauseIds);
    }

    private void validateClauses(Set<Long> clauseIds) {
        if (clauseIds.isEmpty()) {
            return;
        }
        Set<Long> known = company.listClauses().stream().map(Clause::getId).collect(Collectors.toSet());
        if (!known.containsAll(clauseIds)) {
            throw new IllegalArgumentException("Clausula inexistente ou removida");
        }
    }

    private Set<Long> defaultClauseIds() {
        return company.listClauses().stream()
                .filter(Clause::isDefaultClause)
                .map(Clause::getId)
                .collect(Collectors.toSet());
    }

    String nextNumber(LocalDate date) {
        String prefix = date.getYear() + "-";
        String last = repository.findLastNumberWithPrefix(prefix);
        int next = last == null ? 1 : Integer.parseInt(last.substring(prefix.length())) + 1;
        return prefix + String.format("%04d", next);
    }

    private Estimate load(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Orcamento nao encontrado: " + id));
    }

    private EstimateDetails toDetails(Estimate e) {
        Customer c = customers.findById(e.getCustomerId());
        var customer = new EstimateDetails.CustomerSummary(c.getId(), c.getName(), c.getPersonType().name(),
                c.getTaxId(), c.getContactPerson(), c.getAddress(), c.getDistrict(), c.getCity());
        return new EstimateDetails(e.getId(), e.getNumber(), e.getStatus(), customer, e.getTitle(), e.getIssueDate(),
                e.getValidityDays(), e.getMinLeadDays(), e.getMaxLeadDays(), e.getMaterialSupply(),
                e.getEstimatedMaterialCost(), e.getPaymentTerms(), e.getBankAccountId(), Set.copyOf(e.getClauseIds()),
                e.services().stream().map(s -> new EstimateDetails.Service(s.getDescription(), s.getInternalNote())).toList(),
                e.getMaterials().stream().map(m -> new EstimateDetails.Material(m.getDescription(), m.getQuantity(), m.getTotalPrice())).toList(),
                e.getPriceLines().stream().map(p -> new EstimateDetails.Price(p.getDescription(), p.getAmount())).toList(),
                e.getExclusions().stream().map(Exclusion::getContent).toList(),
                e.getInternalNotes().stream().map(InternalNote::getContent).toList(),
                e.getUpdatedAt());
    }

    private static EstimateSummary toSummary(Estimate e, Customer customer) {
        var prices = e.getPriceLines();
        return new EstimateSummary(e.getId(), e.getNumber(), e.getStatus(), e.getCustomerId(),
                customer == null ? null : customer.getName(), e.getTitle(), e.getIssueDate(),
                prices.isEmpty() ? null : prices.getFirst().getAmount(), prices.size(),
                e.getMinLeadDays(), e.getMaxLeadDays(), e.getUpdatedAt());
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }
}
