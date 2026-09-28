package br.com.obracerta.estimate;

import br.com.obracerta.shared.MaterialSupply;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** Orçamento completo, enviado de uma vez pela tela de edição. Listas nulas contam como vazias. */
public record EstimateRequest(

        @NotNull(message = "Cliente e obrigatorio")
        Long customerId,

        @Size(max = 200, message = "Titulo com no maximo 200 caracteres")
        String title,

        LocalDate issueDate,

        @PositiveOrZero(message = "Validade nao pode ser negativa")
        Integer validityDays,

        @PositiveOrZero(message = "Prazo minimo nao pode ser negativo")
        Integer minLeadDays,

        @PositiveOrZero(message = "Prazo maximo nao pode ser negativo")
        Integer maxLeadDays,

        @NotNull(message = "Regime de material e obrigatorio")
        MaterialSupply materialSupply,

        @PositiveOrZero(message = "Valor estimado de material nao pode ser negativo")
        BigDecimal estimatedMaterialCost,

        String paymentTerms,

        Long bankAccountId,

        Set<Long> clauseIds,

        List<@Valid ServiceLine> services,

        List<@Valid MaterialLine> materials,

        List<@Valid PriceLine> priceLines,

        List<@NotBlank(message = "Observacao vazia") String> exclusions,

        List<@NotBlank(message = "Nota interna vazia") String> internalNotes) {

    public record ServiceLine(
            @NotBlank(message = "Descricao do servico e obrigatoria") String description,
            String internalNote) {
    }

    public record MaterialLine(
            @NotBlank(message = "Descricao do material e obrigatoria")
            @Size(max = 200, message = "Material com no maximo 200 caracteres") String description,
            @PositiveOrZero(message = "Quantidade nao pode ser negativa") BigDecimal quantity,
            @PositiveOrZero(message = "Preco do material nao pode ser negativo") BigDecimal totalPrice) {
    }

    public record PriceLine(
            @NotBlank(message = "Descricao do valor e obrigatoria")
            @Size(max = 250, message = "Descricao do valor com no maximo 250 caracteres") String description,
            @NotNull(message = "Valor e obrigatorio")
            @PositiveOrZero(message = "Valor nao pode ser negativo") BigDecimal amount) {
    }
}
