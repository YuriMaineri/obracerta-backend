package br.com.obracerta.catalog;

import br.com.obracerta.shared.Unit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record ServiceRequest(

        @NotBlank(message = "Nome do serviço é obrigatório")
        @Size(max = 200, message = "Nome do serviço com no máximo 200 caracteres")
        String name,

        @NotNull(message = "Categoria é obrigatória")
        ServiceCategory category,

        @NotNull(message = "Unidade do serviço é obrigatória")
        Unit unit,

        @Size(max = 2000, message = "Texto da proposta com no máximo 2000 caracteres")
        String proposalDescription,

        @PositiveOrZero(message = "Diárias por unidade não podem ser negativas")
        BigDecimal daysPerUnit,

        @PositiveOrZero(message = "Preço praticado não pode ser negativo")
        BigDecimal usualPrice,

        List<@Valid CompositionLine> composition) {

    public record CompositionLine(
            @NotNull(message = "Escolha o insumo de cada linha da composição")
            Long materialId,

            @NotNull(message = "Informe o consumo por unidade de cada insumo")
            @Positive(message = "Consumo por unidade deve ser maior que zero")
            BigDecimal quantityPerUnit,

            @PositiveOrZero(message = "Perda não pode ser negativa")
            @DecimalMax(value = "100", message = "Perda de no máximo 100%")
            BigDecimal wastePercent) {
    }
}
