package br.com.obracerta.customers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerServiceTest {

    @Test
    void formattedCnpjIsReducedToDigits() {
        assertThat(CustomerService.normalizeTaxId(Customer.PersonType.COMPANY, "24.839.705/0001-65"))
                .isEqualTo("24839705000165");
    }

    @Test
    void blankTaxIdBecomesNull() {
        assertThat(CustomerService.normalizeTaxId(Customer.PersonType.INDIVIDUAL, "  ")).isNull();
        assertThat(CustomerService.normalizeTaxId(Customer.PersonType.INDIVIDUAL, null)).isNull();
    }

    @Test
    void cpfGivenForCompanyIsRejected() {
        assertThatThrownBy(() -> CustomerService.normalizeTaxId(Customer.PersonType.COMPANY, "123.456.789-09"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CNPJ");
    }
}
