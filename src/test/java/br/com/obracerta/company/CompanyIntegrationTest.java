package br.com.obracerta.company;

import br.com.obracerta.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class CompanyIntegrationTest {

    @Autowired
    CompanyService service;

    @Test
    void seedLoadsMaineriData() {
        Company company = service.getCompany();

        assertThat(company.getTradeName()).isEqualTo("Maineri Elétrica, Construções e Reformas");
        assertThat(company.getTaxId()).isEqualTo("24839705000165");
        assertThat(service.listBankAccounts()).extracting(BankAccount::getBank).containsExactly("Banrisul", "SICREDI");
        assertThat(service.findDefaultBankAccount()).get().extracting(BankAccount::getBank).isEqualTo("Banrisul");
        assertThat(service.listClauses()).hasSize(17);
    }

    @Test
    void onlyOneBankAccountStaysDefault() {
        BankAccount sicredi = service.listBankAccounts().stream()
                .filter(a -> a.getBank().equals("SICREDI")).findFirst().orElseThrow();

        service.updateBankAccount(sicredi.getId(), new BankAccountRequest("SICREDI", "0116", "56780-7",
                "Conta corrente jurídica", null, "Carlos Alberto Maineri da Silva", true));

        List<BankAccount> defaults = service.listBankAccounts().stream().filter(BankAccount::isDefaultAccount).toList();
        assertThat(defaults).extracting(BankAccount::getBank).containsExactly("SICREDI");
    }

    @Test
    void invalidCnpjIsRejected() {
        assertThatThrownBy(() -> service.updateCompany(new CompanyRequest("Maineri", null, "24.839.705/0001-66",
                null, null, null, null, "Porto Alegre", null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void logoMustBePngOrJpeg() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        service.replaceLogo(png);
        assertThat(service.getCompany().getLogoContentType()).isEqualTo("image/png");

        assertThatThrownBy(() -> service.replaceLogo("not an image".getBytes()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deactivatedClauseLeavesTheList() {
        Clause first = service.listClauses().getFirst();

        service.deactivateClause(first.getId());

        assertThat(service.listClauses()).hasSize(16).doesNotContain(first);
    }
}
