package br.com.obracerta.estimate;

import br.com.obracerta.pricing.PriceCalculator;
import br.com.obracerta.shared.Difficulty;
import br.com.obracerta.shared.PricingMode;

import java.math.BigDecimal;

/** Linha de serviço pronta para gravar: texto livre (serviceId nulo) ou ligada ao catálogo, com o cálculo. */
record ServiceEntry(String description, String internalNote, Long serviceId, PricingMode pricingMode,
                    BigDecimal quantity, Difficulty difficulty, PriceCalculator.ItemQuote quote) {

    static ServiceEntry text(String description, String internalNote) {
        return new ServiceEntry(description, internalNote, null, null, null, null, null);
    }
}
