package br.com.obracerta.shared;

/**
 * Regras de documentos brasileiros (CPF e CNPJ).
*/
public final class BrazilianDocuments {

    private BrazilianDocuments() {
    }

    public static String digitsOnly(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    /** CPF valido pelos dois digitos verificadores (modulo 11). */
    public static boolean isValidCpf(String value) {
        String d = digitsOnly(value);
        if (d.length() != 11 || d.chars().distinct().count() == 1) {
            return false;
        }
        return cpfCheckDigit(d, 9) == d.charAt(9) - '0'
                && cpfCheckDigit(d, 10) == d.charAt(10) - '0';
    }

    /** CNPJ valido pelos dois digitos verificadores (modulo 11 com pesos de 2 a 9). */
    public static boolean isValidCnpj(String value) {
        String d = digitsOnly(value);
        if (d.length() != 14 || d.chars().distinct().count() == 1) {
            return false;
        }
        return cnpjCheckDigit(d, 12) == d.charAt(12) - '0'
                && cnpjCheckDigit(d, 13) == d.charAt(13) - '0';
    }

    private static int cpfCheckDigit(String d, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += (d.charAt(i) - '0') * (length + 1 - i);
        }
        return (sum * 10) % 11 % 10;
    }

    private static int cnpjCheckDigit(String d, int length) {
        int sum = 0;
        int weight = length - 7;
        for (int i = 0; i < length; i++) {
            sum += (d.charAt(i) - '0') * weight--;
            if (weight < 2) {
                weight = 9;
            }
        }
        int rest = sum % 11;
        return rest < 2 ? 0 : 11 - rest;
    }
}
