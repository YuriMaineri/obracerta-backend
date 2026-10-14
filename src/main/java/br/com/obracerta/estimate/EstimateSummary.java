package br.com.obracerta.estimate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Linha da listagem. mainAmount é o primeiro valor que não é alternativa, geralmente o da mão de obra. */
public record EstimateSummary(
        Long id,
        String number,
        EstimateStatus status,
        Long customerId,
        String customerName,
        String title,
        LocalDate issueDate,
        BigDecimal mainAmount,
        int priceLineCount,
        Integer minLeadDays,
        Integer maxLeadDays,
        Instant updatedAt) {
}
