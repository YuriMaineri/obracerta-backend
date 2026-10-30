package br.com.obracerta.pricing;

import br.com.obracerta.shared.Difficulty;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

/** Valor da diária, custo indireto, margem e fatores de dificuldade. Existe uma única linha. */
@Entity
@Table(name = "pricing_settings")
public class PricingSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal dailyRate;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal indirectPercent;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal marginPercent;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal easyFactor;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal normalFactor;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal hardFactor;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal veryHardFactor;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    protected PricingSettings() {
    }

    void update(PricingSettingsRequest r) {
        this.dailyRate = r.dailyRate();
        this.indirectPercent = r.indirectPercent();
        this.marginPercent = r.marginPercent();
        this.easyFactor = r.easyFactor();
        this.normalFactor = r.normalFactor();
        this.hardFactor = r.hardFactor();
        this.veryHardFactor = r.veryHardFactor();
        this.updatedAt = Instant.now();
    }

    public Rates toRates() {
        Map<Difficulty, BigDecimal> factors = new EnumMap<>(Difficulty.class);
        factors.put(Difficulty.EASY, easyFactor);
        factors.put(Difficulty.NORMAL, normalFactor);
        factors.put(Difficulty.HARD, hardFactor);
        factors.put(Difficulty.VERY_HARD, veryHardFactor);
        return new Rates(dailyRate, indirectPercent, marginPercent, factors);
    }

    public BigDecimal getDailyRate() { return dailyRate; }
    public BigDecimal getIndirectPercent() { return indirectPercent; }
    public BigDecimal getMarginPercent() { return marginPercent; }
    public BigDecimal getEasyFactor() { return easyFactor; }
    public BigDecimal getNormalFactor() { return normalFactor; }
    public BigDecimal getHardFactor() { return hardFactor; }
    public BigDecimal getVeryHardFactor() { return veryHardFactor; }
    public Instant getUpdatedAt() { return updatedAt; }
}
