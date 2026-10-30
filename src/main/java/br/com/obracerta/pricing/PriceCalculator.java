package br.com.obracerta.pricing;

import br.com.obracerta.shared.Difficulty;
import br.com.obracerta.shared.MaterialSupply;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Preço sugerido: mão de obra = quantidade × diárias por unidade × fator de dificuldade × diária;
 * sobre mão de obra + material entram o custo indireto e depois a margem.
 */
public final class PriceCalculator {

    private PriceCalculator() {
    }

    public record ItemQuote(BigDecimal laborDays, BigDecimal laborCost, BigDecimal materialCost, BigDecimal price) {
    }

    public record Quote(BigDecimal laborDays, BigDecimal laborCost, BigDecimal materialCost, boolean materialCharged,
                        BigDecimal indirectCost, BigDecimal margin, BigDecimal total) {
    }

    /** Material do cliente ou comprado por nota não entra no preço. */
    public static boolean chargesMaterial(MaterialSupply supply) {
        return supply == MaterialSupply.ITEMIZED || supply == MaterialSupply.ESTIMATED;
    }

    public static ItemQuote item(Rates rates, BigDecimal quantity, BigDecimal daysPerUnit,
                                 BigDecimal materialCostPerUnit, Difficulty difficulty, boolean chargeMaterial) {
        BigDecimal days = quantity.multiply(orZero(daysPerUnit)).multiply(rates.factor(difficulty));
        BigDecimal labor = money(days.multiply(rates.dailyRate()));
        BigDecimal material = money(quantity.multiply(orZero(materialCostPerUnit)));
        BigDecimal base = chargeMaterial ? labor.add(material) : labor;
        BigDecimal price = money(base.multiply(markup(rates.indirectPercent())).multiply(markup(rates.marginPercent())));
        return new ItemQuote(days.setScale(3, RoundingMode.HALF_UP), labor, material, price);
    }

    public static Quote total(Rates rates, List<ItemQuote> items, boolean chargeMaterial) {
        BigDecimal days = items.stream().map(ItemQuote::laborDays).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal labor = items.stream().map(ItemQuote::laborCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal material = items.stream().map(ItemQuote::materialCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal direct = chargeMaterial ? labor.add(material) : labor;
        BigDecimal indirect = money(direct.multiply(rates.indirectPercent()).movePointLeft(2));
        BigDecimal margin = money(direct.add(indirect).multiply(rates.marginPercent()).movePointLeft(2));
        return new Quote(days, labor, material, chargeMaterial, indirect, margin, direct.add(indirect).add(margin));
    }

    private static BigDecimal markup(BigDecimal percent) {
        return BigDecimal.ONE.add(percent.movePointLeft(2));
    }

    private static BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
