package br.com.obracerta.estimate;

import br.com.obracerta.shared.Difficulty;
import br.com.obracerta.shared.MaterialSupply;
import br.com.obracerta.shared.Unit;

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
        List<ShoppingItem> shoppingList,
        Instant updatedAt) {

    public record CustomerSummary(Long id, String name, String personType, String taxId, String contactPerson,
                                  String address, String district, String city, String postalCode) {
    }

    /**
     * serviceId nulo = texto livre, sem cálculo. suggestedPrice já inclui custo indireto e margem.
     * laborDays, laborCost, materialCost e materials são a memória de cálculo do momento em que foi salvo.
     */
    public record Service(String description, String internalNote, Long serviceId, BigDecimal quantity,
                          Difficulty difficulty, BigDecimal suggestedPrice, BigDecimal laborDays,
                          BigDecimal laborCost, BigDecimal materialCost, List<MaterialUse> materials) {
    }

    /** Insumo de um serviço: quantity é o consumo já com a perda; cost nulo = insumo sem preço. */
    public record MaterialUse(Long materialId, String material, Unit unit, String packageName, BigDecimal packageSize,
                              BigDecimal quantityPerUnit, BigDecimal wastePercent, BigDecimal quantity,
                              BigDecimal unitPrice, BigDecimal cost) {
    }

    /**
     * Um insumo somado em todos os serviços. cost é o que entra no preço (pelo consumo); purchaseCost é o que se
     * paga comprando embalagens inteiras. packages nulo = comprado fracionado.
     */
    public record ShoppingItem(Long materialId, String material, Unit unit, String packageName, BigDecimal packageSize,
                               BigDecimal needed, BigDecimal packages, BigDecimal bought, BigDecimal leftover,
                               BigDecimal unitPrice, BigDecimal cost, BigDecimal purchaseCost) {
    }

    /** materialId preenchido = linha trazida da lista de compra dos serviços; nulo = digitada à mão. */
    public record Material(String description, BigDecimal quantity, BigDecimal totalPrice, Long materialId) {
    }

    public record Price(String description, BigDecimal amount, boolean alternative) {
    }
}
