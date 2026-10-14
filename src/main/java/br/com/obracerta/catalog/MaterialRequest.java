package br.com.obracerta.catalog;

import br.com.obracerta.shared.Unit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MaterialRequest(

        @NotBlank(message = "Nome do insumo é obrigatório")
        @Size(max = 200, message = "Nome do insumo com no máximo 200 caracteres")
        String name,

        @NotNull(message = "Unidade do insumo é obrigatória")
        Unit unit,

        @PositiveOrZero(message = "Preço não pode ser negativo")
        BigDecimal price,

        @Size(max = 150, message = "Fornecedor com no máximo 150 caracteres")
        String supplier) {
}
