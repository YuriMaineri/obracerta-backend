package br.com.obracerta.customers;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados de entrada para criar ou atualizar um cliente.
 */
public record CustomerRequest(

        @NotNull(message = "Informe se e pessoa fisica ou juridica")
        Customer.PersonType personType,

        @NotBlank(message = "Nome e obrigatorio")
        @Size(max = 200, message = "Nome com no maximo 200 caracteres")
        String name,

        String taxId,

        @Size(max = 150, message = "Contato com no maximo 150 caracteres")
        String contactPerson,

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

        String postalCode) {
}
