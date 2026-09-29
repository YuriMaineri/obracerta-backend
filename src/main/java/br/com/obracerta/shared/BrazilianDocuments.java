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

    /** Telefone com DDD (10 ou 11 digitos), guardado so com digitos. Vazio vira null. */
    public static String normalizePhone(String phone) {
        String digits = digitsOnly(phone);
        if (digits.isEmpty()) {
            return null;
        }
        if (digits.length() != 10 && digits.length() != 11) {
            throw new IllegalArgumentException("Telefone deve ter DDD e 8 ou 9 digitos, recebido: " + phone);
        }
        return digits;
    }

    /** CEP com 8 digitos, guardado so com digitos. Vazio vira null. */
    public static String normalizePostalCode(String postalCode) {
        String digits = digitsOnly(postalCode);
        if (digits.isEmpty()) {
            return null;
        }
        if (digits.length() != 8) {
            throw new IllegalArgumentException("CEP deve ter 8 digitos, recebido: " + postalCode);
        }
        return digits;
    }

    /** CNPJ obrigatoriamente valido, guardado so com digitos. Vazio vira null. */
    public static String normalizeCnpj(String cnpj) {
        String digits = digitsOnly(cnpj);
        if (digits.isEmpty()) {
            return null;
        }
        if (!isValidCnpj(digits)) {
            throw new IllegalArgumentException("CNPJ invalido: " + cnpj);
        }
        return digits;
    }

    /** CNPJ ou CPF formatado conforme a quantidade de dígitos; outros valores voltam como estão. */
    public static String formatTaxId(String taxId) {
        String d = digitsOnly(taxId);
        if (d.length() == 14) {
            return "%s.%s.%s/%s-%s".formatted(d.substring(0, 2), d.substring(2, 5), d.substring(5, 8), d.substring(8, 12), d.substring(12));
        }
        if (d.length() == 11) {
            return "%s.%s.%s-%s".formatted(d.substring(0, 3), d.substring(3, 6), d.substring(6, 9), d.substring(9));
        }
        return taxId;
    }

    public static String formatPhone(String phone) {
        String d = digitsOnly(phone);
        if (d.length() == 11) {
            return "(%s) %s-%s".formatted(d.substring(0, 2), d.substring(2, 7), d.substring(7));
        }
        if (d.length() == 10) {
            return "(%s) %s-%s".formatted(d.substring(0, 2), d.substring(2, 6), d.substring(6));
        }
        return phone;
    }

    public static String formatPostalCode(String postalCode) {
        String d = digitsOnly(postalCode);
        return d.length() == 8 ? d.substring(0, 5) + "-" + d.substring(5) : postalCode;
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
