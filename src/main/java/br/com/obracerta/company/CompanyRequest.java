package br.com.obracerta.company;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRequest(

        @NotBlank(message = "Razao social e obrigatoria")
        @Size(max = 200, message = "Razao social com no maximo 200 caracteres")
        String legalName,

        @Size(max = 200, message = "Nome fantasia com no maximo 200 caracteres")
        String tradeName,

        String taxId,

        String phone,

        @Email(regexp = "^$|^[^@\\s]+@[^@\\s]+\\.[a-zA-Z]{2,}$", message = "E-mail invalido")
        @Size(max = 150, message = "E-mail com no maximo 150 caracteres")
        String email,

        @Size(max = 250, message = "Endereco com no maximo 250 caracteres")
        String address,

        @Size(max = 100, message = "Bairro com no maximo 100 caracteres")
        String district,

        @Size(max = 100, message = "Cidade com no maximo 100 caracteres")
        String city,

        String postalCode,

        @Size(max = 150, message = "Nome do responsavel com no maximo 150 caracteres")
        String signatoryName) {
}
