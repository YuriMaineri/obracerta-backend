package br.com.obracerta.estimate;

import br.com.obracerta.TestcontainersConfiguration;
import br.com.obracerta.company.BankAccount;
import br.com.obracerta.company.Clause;
import br.com.obracerta.company.CompanyService;
import br.com.obracerta.customers.Customer;
import br.com.obracerta.customers.CustomerRequest;
import br.com.obracerta.customers.CustomerService;
import br.com.obracerta.shared.MaterialSupply;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class EstimateIntegrationTest {

    @Autowired
    EstimateService service;

    @Autowired
    CustomerService customers;

    @Autowired
    CompanyService company;

    Long customerId;

    @BeforeEach
    void createCustomer() {
        customerId = customers.create(new CustomerRequest(Customer.PersonType.COMPANY, "Cartório de Registro Civil 2ª Zona",
                null, null, null, null, "Rua Venâncio Aires, 243", "Cidade Baixa", "Porto Alegre", null)).getId();
    }

    @Test
    void newEstimateGetsNumberDefaultAccountAndDefaultClauses() {
        EstimateDetails created = service.create(cartorio(null));

        assertThat(created.number()).matches(LocalDate.now().getYear() + "-\\d{4}");
        assertThat(created.status()).isEqualTo(EstimateStatus.DRAFT);
        assertThat(created.bankAccountId()).isEqualTo(company.findDefaultBankAccount().map(BankAccount::getId).orElseThrow());
        long defaults = company.listClauses().stream().filter(Clause::isDefaultClause).count();
        assertThat(created.clauseIds()).hasSize((int) defaults);
        assertThat(created.services()).extracting(EstimateDetails.Service::description)
                .containsExactly("Fazer fechamento de gesso", "Pintura do teto da sala arquivo");
        assertThat(created.services().getFirst().internalNote()).isEqualTo("Ver com o gesseiro");
        assertThat(created.priceLines()).extracting(EstimateDetails.Price::amount)
                .containsExactly(new BigDecimal("1800.00"), new BigDecimal("900.00"));
    }

    @Test
    void numbersAreSequentialWithinTheYear() {
        String first = service.create(cartorio(null)).number();
        String second = service.create(cartorio(null)).number();

        int a = Integer.parseInt(first.substring(5));
        int b = Integer.parseInt(second.substring(5));
        assertThat(b).isEqualTo(a + 1);
    }

    @Test
    void updateReplacesAllLines() {
        EstimateDetails created = service.create(cartorio(null));

        EstimateRequest changed = new EstimateRequest(customerId, "Porta do servidor", null, null, 4, 5,
                MaterialSupply.CUSTOMER_SUPPLIED, null, null, created.bankAccountId(), created.clauseIds(),
                List.of(new EstimateRequest.ServiceLine("Instalação de uma luminária", null)),
                List.of(), List.of(new EstimateRequest.PriceLine("Valor da mão de obra", new BigDecimal("2000"))),
                List.of("Não incluso reparo de pintura"), List.of());
        EstimateDetails updated = service.update(created.id(), changed);

        assertThat(updated.services()).hasSize(1);
        assertThat(updated.materials()).isEmpty();
        assertThat(updated.exclusions()).containsExactly("Não incluso reparo de pintura");
        assertThat(updated.internalNotes()).isEmpty();
        assertThat(updated.materialSupply()).isEqualTo(MaterialSupply.CUSTOMER_SUPPLIED);
    }

    @Test
    void duplicateCopiesContentWithNewNumberAsDraft() {
        EstimateDetails original = service.changeStatus(service.create(cartorio(null)).id(), EstimateStatus.SENT);

        EstimateDetails copy = service.duplicate(original.id());

        assertThat(copy.number()).isNotEqualTo(original.number());
        assertThat(copy.status()).isEqualTo(EstimateStatus.DRAFT);
        assertThat(copy.services()).isEqualTo(original.services());
        assertThat(copy.priceLines()).isEqualTo(original.priceLines());
        assertThat(copy.clauseIds()).isEqualTo(original.clauseIds());
    }

    @Test
    void onlyDraftsCanBeDeleted() {
        EstimateDetails sent = service.changeStatus(service.create(cartorio(null)).id(), EstimateStatus.SENT);

        assertThatThrownBy(() -> service.delete(sent.id())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void searchFiltersByCustomer() {
        service.create(cartorio("Porta servidor e pintura"));

        var page = service.search("servidor", null, customerId, Pageable.unpaged());

        assertThat(page.getContent()).singleElement().satisfies(s -> {
            assertThat(s.customerName()).isEqualTo("Cartório de Registro Civil 2ª Zona");
            assertThat(s.mainAmount()).isEqualByComparingTo("1800");
        });
    }

    @Test
    void minLeadDaysCannotExceedMax() {
        EstimateRequest invalid = new EstimateRequest(customerId, null, null, null, 6, 4, MaterialSupply.ITEMIZED,
                null, null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> service.create(invalid)).isInstanceOf(IllegalArgumentException.class);
    }

    private EstimateRequest cartorio(String title) {
        return new EstimateRequest(customerId, title, null, null, 4, 5, MaterialSupply.ESTIMATED, null,
                null, null, null,
                List.of(new EstimateRequest.ServiceLine("Fazer fechamento de gesso", "Ver com o gesseiro"),
                        new EstimateRequest.ServiceLine("Pintura do teto da sala arquivo", null)),
                List.of(new EstimateRequest.MaterialLine("Fios cabo flex 2,5mm", null, null)),
                List.of(new EstimateRequest.PriceLine("Valor da mão de obra de elétrica e pintura", new BigDecimal("1800.00")),
                        new EstimateRequest.PriceLine("Valor de material para pintura e elétrica", new BigDecimal("900.00"))),
                List.of(), List.of("Cliente pediu para começar numa segunda"));
    }
}
