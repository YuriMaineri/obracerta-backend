package br.com.obracerta.shared;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
