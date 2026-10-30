package br.com.obracerta.pricing;

import br.com.obracerta.shared.Difficulty;

import java.math.BigDecimal;
import java.util.Map;

/** Parâmetros do cálculo, separados da entidade para o calculador ser puro. */
public record Rates(BigDecimal dailyRate, BigDecimal indirectPercent, BigDecimal marginPercent,
                    Map<Difficulty, BigDecimal> factors) {

    public BigDecimal factor(Difficulty difficulty) {
        return factors.get(difficulty == null ? Difficulty.NORMAL : difficulty);
    }
}
