package br.com.obracerta.estimate;

import br.com.obracerta.shared.Difficulty;
import br.com.obracerta.shared.MaterialSupply;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** Orçamento completo, na visão interna (notas internas e preço sugerido). Base da tela e do PDF. */
public record EstimateDetails(
        Long id,
        String number,
        EstimateStatus status,
        CustomerSummary customer,
        String title,
        LocalDate issueDate,
        Integer validityDays,
        Integer minLeadDays,
        Integer maxLeadDays,
        MaterialSupply materialSupply,
        BigDecimal estimatedMaterialCost,
        String paymentTerms,
        Long bankAccountId,
        Set<Long> clauseIds,
        List<Service> services,
        List<Material> materials,
        List<Price> priceLines,
        List<String> exclusions,
        List<String> internalNotes,
        BigDecimal suggestedTotal,
        BigDecimal suggestedDays,
        Instant updatedAt) {

    public record CustomerSummary(Long id, String name, String personType, String taxId, String contactPerson,
                                  String address, String district, String city, String postalCode) {
    }

    /** serviceId nulo = texto livre, sem cálculo. suggestedPrice já inclui custo indireto e margem. */
    public record Service(String description, String internalNote, Long serviceId, BigDecimal quantity,
                          Difficulty difficulty, BigDecimal suggestedPrice) {
    }

    public record Material(String description, BigDecimal quantity, BigDecimal totalPrice) {
    }

    public record Price(String description, BigDecimal amount, boolean alternative) {
    }
}
