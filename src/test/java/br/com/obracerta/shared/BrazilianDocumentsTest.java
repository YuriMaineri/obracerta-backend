package br.com.obracerta.shared;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrazilianDocumentsTest {

    @Test
    void validCpfWithOrWithoutMask() {
        assertThat(BrazilianDocuments.isValidCpf("529.982.247-25")).isTrue();
        assertThat(BrazilianDocuments.isValidCpf("52998224725")).isTrue();
    }

    @Test
    void cpfWithWrongCheckDigitOrRepeatedDigitsIsInvalid() {
        assertThat(BrazilianDocuments.isValidCpf("529.982.247-26")).isFalse();
        assertThat(BrazilianDocuments.isValidCpf("111.111.111-11")).isFalse();
    }

    @Test
    void maineriCnpjIsValid() {
        assertThat(BrazilianDocuments.isValidCnpj("24.839.705/0001-65")).isTrue();
    }

    @Test
    void cnpjWithWrongCheckDigitOrRepeatedDigitsIsInvalid() {
        assertThat(BrazilianDocuments.isValidCnpj("24.839.705/0001-66")).isFalse();
        assertThat(BrazilianDocuments.isValidCnpj("00.000.000/0000-00")).isFalse();
    }

    @Test
    void phoneKeepsOnlyDigitsAndRequiresAreaCode() {
        assertThat(BrazilianDocuments.normalizePhone("(51) 99999-8888")).isEqualTo("51999998888");
        assertThat(BrazilianDocuments.normalizePhone("(51) 3333-4444")).isEqualTo("5133334444");
        assertThat(BrazilianDocuments.normalizePhone("")).isNull();
        assertThatThrownBy(() -> BrazilianDocuments.normalizePhone("9999-8888"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void postalCodeKeepsOnlyDigitsAndRequiresEight() {
        assertThat(BrazilianDocuments.normalizePostalCode("90010-000")).isEqualTo("90010000");
        assertThatThrownBy(() -> BrazilianDocuments.normalizePostalCode("9001-000"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cnpjIsNormalizedOrRejected() {
        assertThat(BrazilianDocuments.normalizeCnpj("24.839.705/0001-65")).isEqualTo("24839705000165");
        assertThat(BrazilianDocuments.normalizeCnpj(null)).isNull();
        assertThatThrownBy(() -> BrazilianDocuments.normalizeCnpj("24.839.705/0001-66"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void formatsDocumentsPhoneAndPostalCode() {
        assertThat(BrazilianDocuments.formatTaxId("24839705000165")).isEqualTo("24.839.705/0001-65");
        assertThat(BrazilianDocuments.formatTaxId("52998224725")).isEqualTo("529.982.247-25");
        assertThat(BrazilianDocuments.formatPhone("51984232827")).isEqualTo("(51) 98423-2827");
        assertThat(BrazilianDocuments.formatPhone("5133334444")).isEqualTo("(51) 3333-4444");
        assertThat(BrazilianDocuments.formatPostalCode("90420090")).isEqualTo("90420-090");
    }
}
