package br.com.obracerta.pricing;

import br.com.obracerta.shared.Unit;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * O que comprar de um insumo para cobrir o consumo do orçamento. Com embalagem, arredonda para embalagens
 * inteiras (5,04 L em galão de 3,6 L = 2 galões); sem embalagem, arredonda só o que se compra inteiro (saco, peça).
 * packages é nulo quando nada é arredondado. Não entra no preço sugerido: serve para conferir a compra.
 */
public record Purchase(BigDecimal packages, BigDecimal bought, BigDecimal leftover, BigDecimal cost) {

    public static Purchase of(Unit unit, BigDecimal packageSize, BigDecimal needed, BigDecimal price) {
        BigDecimal packages = null;
        BigDecimal bought = needed;
        if (packageSize != null) {
            packages = needed.divide(packageSize, 6, RoundingMode.HALF_UP).setScale(0, RoundingMode.CEILING);
            bought = packages.multiply(packageSize);
        } else if (unit.soldWhole()) {
            packages = needed.setScale(0, RoundingMode.CEILING);
            bought = packages;
        }
        BigDecimal cost = price == null ? null : bought.multiply(price).setScale(2, RoundingMode.HALF_UP);
        return new Purchase(packages, bought.stripTrailingZeros(), bought.subtract(needed).stripTrailingZeros(), cost);
    }
}
