package br.com.obracerta.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BankAccountRequest(

        @NotBlank(message = "Banco e obrigatorio")
        @Size(max = 100, message = "Banco com no maximo 100 caracteres")
        String bank,

        @Size(max = 20, message = "Agencia com no maximo 20 caracteres")
        String branch,

        @Size(max = 30, message = "Conta com no maximo 30 caracteres")
        String accountNumber,

        @Size(max = 30, message = "Tipo de conta com no maximo 30 caracteres")
        String accountType,

        @Size(max = 150, message = "Chave PIX com no maximo 150 caracteres")
        String pixKey,

        @Size(max = 200, message = "Titular com no maximo 200 caracteres")
        String holder,

        boolean defaultAccount) {
}
