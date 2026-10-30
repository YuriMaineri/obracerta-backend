package br.com.obracerta.pricing;

import br.com.obracerta.shared.Difficulty;
import br.com.obracerta.shared.MaterialSupply;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PriceCalculatorTest {

    Rates rates = new Rates(new BigDecimal("350.00"), new BigDecimal("10"), new BigDecimal("15"), Map.of(
            Difficulty.EASY, new BigDecimal("0.85"), Difficulty.NORMAL, BigDecimal.ONE,
            Difficulty.HARD, new BigDecimal("1.25"), Difficulty.VERY_HARD, new BigDecimal("1.5")));

    @Test
    void wallPaintingOfThirtySquareMetersHard() {
        var item = PriceCalculator.item(rates, new BigDecimal("30"), new BigDecimal("0.0357"),
                new BigDecimal("4.46"), Difficulty.HARD, true);

        assertThat(item.laborDays()).isEqualByComparingTo("1.339");
        assertThat(item.laborCost()).isEqualByComparingTo("468.56");
        assertThat(item.materialCost()).isEqualByComparingTo("133.80");
        assertThat(item.price()).isEqualByComparingTo("761.99");

        var quote = PriceCalculator.total(rates, List.of(item), true);
        assertThat(quote.indirectCost()).isEqualByComparingTo("60.24");
        assertThat(quote.margin()).isEqualByComparingTo("99.39");
        assertThat(quote.total()).isEqualByComparingTo("761.99");
    }

    @Test
    void materialSuppliedByCustomerStaysOutOfThePrice() {
        var item = PriceCalculator.item(rates, new BigDecimal("30"), new BigDecimal("0.0357"),
                new BigDecimal("4.46"), null, false);

        assertThat(item.laborCost()).isEqualByComparingTo("374.85");
        assertThat(item.price()).isEqualByComparingTo("474.19");
        assertThat(PriceCalculator.total(rates, List.of(item), false).total()).isEqualByComparingTo("474.19");
        assertThat(PriceCalculator.chargesMaterial(MaterialSupply.CUSTOMER_SUPPLIED)).isFalse();
        assertThat(PriceCalculator.chargesMaterial(MaterialSupply.BY_RECEIPT)).isFalse();
        assertThat(PriceCalculator.chargesMaterial(MaterialSupply.ITEMIZED)).isTrue();
    }
}
