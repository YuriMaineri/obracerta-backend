package br.com.obracerta.customers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Dados de entrada para criar ou atualizar um cliente.
 */
public record CustomerRequest(

        @NotNull Customer.PersonType personType,
        @NotBlank String name,
        String taxId,
        String contactPerson,
        String phone,
        String email,
        String address,
        String district,
        String city,
        String postalCode) {
}
