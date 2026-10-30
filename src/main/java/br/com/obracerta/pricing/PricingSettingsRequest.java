package br.com.obracerta.pricing;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PricingSettingsRequest(

        @NotNull(message = "Valor da diária é obrigatório")
        @PositiveOrZero(message = "Valor da diária não pode ser negativo")
        BigDecimal dailyRate,

        @NotNull(message = "Custo indireto é obrigatório")
        @DecimalMin(value = "0", message = "Custo indireto não pode ser negativo")
        @DecimalMax(value = "100", message = "Custo indireto de no máximo 100%")
        BigDecimal indirectPercent,

        @NotNull(message = "Margem é obrigatória")
        @DecimalMin(value = "0", message = "Margem não pode ser negativa")
        @DecimalMax(value = "100", message = "Margem de no máximo 100%")
        BigDecimal marginPercent,

        @NotNull(message = "Fator de serviço fácil é obrigatório")
        @DecimalMin(value = "0.1", message = "Fator mínimo é 0,1")
        @DecimalMax(value = "5", message = "Fator máximo é 5")
        BigDecimal easyFactor,

        @NotNull(message = "Fator de serviço normal é obrigatório")
        @DecimalMin(value = "0.1", message = "Fator mínimo é 0,1")
        @DecimalMax(value = "5", message = "Fator máximo é 5")
        BigDecimal normalFactor,

        @NotNull(message = "Fator de serviço difícil é obrigatório")
        @DecimalMin(value = "0.1", message = "Fator mínimo é 0,1")
        @DecimalMax(value = "5", message = "Fator máximo é 5")
        BigDecimal hardFactor,

        @NotNull(message = "Fator de serviço muito difícil é obrigatório")
        @DecimalMin(value = "0.1", message = "Fator mínimo é 0,1")
        @DecimalMax(value = "5", message = "Fator máximo é 5")
        BigDecimal veryHardFactor) {
}
