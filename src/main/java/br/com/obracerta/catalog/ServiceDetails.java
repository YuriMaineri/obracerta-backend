package br.com.obracerta.catalog;

import br.com.obracerta.shared.PricingMode;
import br.com.obracerta.shared.Unit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/** Serviço com o custo de material por unidade já calculado; é o que o motor do orçamento consome. */
public record ServiceDetails(
        Long id,
        String name,
        ServiceCategory category,
        Unit unit,
        PricingMode pricingMode,
        String proposalDescription,
        BigDecimal daysPerUnit,
        BigDecimal usualPrice,
        List<Line> composition,
        BigDecimal materialCostPerUnit,
        boolean missingPrices) {

    /** costPerUnit = consumo × (1 + perda) × preço do insumo. Nulo quando o insumo está sem preço. */
    public record Line(Long materialId, String materialName, Unit materialUnit, BigDecimal materialPrice,
                       BigDecimal quantityPerUnit, BigDecimal wastePercent, BigDecimal costPerUnit) {
    }

    static ServiceDetails of(Service s) {
        List<Line> lines = s.getComposition().stream().map(ServiceDetails::line).toList();
        BigDecimal total = lines.stream().map(Line::costPerUnit).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean missing = lines.stream().anyMatch(l -> l.costPerUnit() == null);
        return new ServiceDetails(s.getId(), s.getName(), s.getCategory(), s.getUnit(), s.getPricingMode(),
                s.getProposalDescription(), s.getDaysPerUnit(), s.getUsualPrice(), lines, total, missing);
    }

    static BigDecimal lineCost(BigDecimal quantityPerUnit, BigDecimal wastePercent, BigDecimal price) {
        if (price == null) {
            return null;
        }
        BigDecimal waste = BigDecimal.ONE.add(wastePercent.movePointLeft(2));
        return quantityPerUnit.multiply(waste).multiply(price).setScale(2, RoundingMode.HALF_UP);
    }

    private static Line line(ServiceMaterial c) {
        Material m = c.getMaterial();
        return new Line(m.getId(), m.getName(), m.getUnit(), m.getPrice(), c.getQuantityPerUnit(),
                c.getWastePercent(), lineCost(c.getQuantityPerUnit(), c.getWastePercent(), m.getPrice()));
    }
}
