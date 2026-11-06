package br.com.obracerta.pricing;

import br.com.obracerta.shared.Unit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseTest {

    @Test
    void packagedMaterialRoundsUpToWholePackages() {
        // 30 m² de pintura: 30 × 0,16 L × 1,05 = 5,04 L, em galão de 3,6 L
        var paint = Purchase.of(Unit.LITER, new BigDecimal("3.6"), new BigDecimal("5.04"), new BigDecimal("24.00"));

        assertThat(paint.packages()).isEqualByComparingTo("2");
        assertThat(paint.bought()).isEqualByComparingTo("7.2");
        assertThat(paint.leftover()).isEqualByComparingTo("2.16");
        assertThat(paint.cost()).isEqualByComparingTo("172.80");

        var exact = Purchase.of(Unit.LITER, new BigDecimal("3.6"), new BigDecimal("7.2"), new BigDecimal("24.00"));
        assertThat(exact.packages()).isEqualByComparingTo("2");
        assertThat(exact.leftover()).isEqualByComparingTo("0");
    }

    @Test
    void wholeUnitsRoundUpAndFractionalUnitsDoNot() {
        var cement = Purchase.of(Unit.BAG, null, new BigDecimal("2.3"), new BigDecimal("38.00"));
        assertThat(cement.packages()).isEqualByComparingTo("3");
        assertThat(cement.leftover()).isEqualByComparingTo("0.7");
        assertThat(cement.cost()).isEqualByComparingTo("114.00");

        var sand = Purchase.of(Unit.CUBIC_METER, null, new BigDecimal("0.4125"), new BigDecimal("160.00"));
        assertThat(sand.packages()).isNull();
        assertThat(sand.bought()).isEqualByComparingTo("0.4125");
        assertThat(sand.leftover()).isEqualByComparingTo("0");
        assertThat(sand.cost()).isEqualByComparingTo("66.00");

        assertThat(Purchase.of(Unit.PIECE, null, BigDecimal.ONE, null).cost()).isNull();
    }

    @Test
    void materialUseOfAnItem() {
        // tinta: 0,16 L/m² com 5% de perda; custo por m² da composição R$ 4,03
        var use = PriceCalculator.material(new BigDecimal("30"), new BigDecimal("0.16"), new BigDecimal("5"),
                new BigDecimal("4.03"));

        assertThat(use.quantity()).isEqualByComparingTo("5.04");
        assertThat(use.cost()).isEqualByComparingTo("120.90");
        assertThat(PriceCalculator.material(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO, null).cost()).isNull();
    }
}
