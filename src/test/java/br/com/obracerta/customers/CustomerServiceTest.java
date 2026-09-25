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
        assertThatThrownBy(() -> CustomerService.normalizeTaxId(Customer.PersonType.COMPANY, "529.982.247-25"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CNPJ");
    }

    @Test
    void cnpjWithWrongCheckDigitIsRejected() {
        assertThatThrownBy(() -> CustomerService.normalizeTaxId(Customer.PersonType.COMPANY, "24.839.705/0001-66"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CNPJ invalido");
    }

    @Test
    void phoneKeepsOnlyDigitsAndRequiresAreaCode() {
        assertThat(CustomerService.normalizePhone("(51) 99999-8888")).isEqualTo("51999998888");
        assertThat(CustomerService.normalizePhone("(51) 3333-4444")).isEqualTo("5133334444");
        assertThat(CustomerService.normalizePhone("")).isNull();
        assertThatThrownBy(() -> CustomerService.normalizePhone("9999-8888"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void postalCodeKeepsOnlyDigitsAndRequiresEight() {
        assertThat(CustomerService.normalizePostalCode("90010-000")).isEqualTo("90010000");
        assertThatThrownBy(() -> CustomerService.normalizePostalCode("9001-000"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
