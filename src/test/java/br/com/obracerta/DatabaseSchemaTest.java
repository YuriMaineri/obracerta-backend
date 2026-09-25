package br.com.obracerta;

import br.com.obracerta.customers.Customer;
import br.com.obracerta.customers.CustomerRequest;
import br.com.obracerta.customers.CustomerService;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/** Sobe a aplicação num PostgreSQL real: aplica as migrations e valida as entidades contra o schema. */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class DatabaseSchemaTest {

    @Autowired
    Flyway flyway;

    @Autowired
    CustomerService customerService;

    @Test
    void allMigrationsAreAppliedAndValid() {
        assertThat(flyway.info().pending()).isEmpty();
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
    }

    @Test
    void customerIsPersistedWithDigitsOnly() {
        Customer saved = customerService.create(new CustomerRequest(
                Customer.PersonType.COMPANY, "Cartório Registro Civil 2ª Zona", "24.839.705/0001-65",
                "Sra. Teste", "(51) 3333-4444", "contato@cartorio.com.br",
                "Rua dos Andradas, 100", "Centro", "Porto Alegre", "90020-000"));

        Customer found = customerService.findById(saved.getId());

        assertThat(found.getTaxId()).isEqualTo("24839705000165");
        assertThat(found.getPhone()).isEqualTo("5133334444");
        assertThat(found.getPostalCode()).isEqualTo("90020000");
    }
}
