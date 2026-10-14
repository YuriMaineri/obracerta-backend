package br.com.obracerta.estimate;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Linha de "VALOR DA MÃO DE OBRA E MATERIAL" (ex.: "Valor da mão de obra", R$ 1.800,00). Alternativas ficam fora do total. */
@Entity
@Table(name = "estimate_price_line")
class EstimatePriceLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estimate_id")
    private Estimate estimate;

    @Column(nullable = false, length = 250)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean alternative;

    protected EstimatePriceLine() {
    }

    EstimatePriceLine(Estimate estimate, String description, BigDecimal amount, boolean alternative, int sortOrder) {
        this.estimate = estimate;
        this.description = description;
        this.amount = amount;
        this.alternative = alternative;
        this.sortOrder = sortOrder;
    }

    String getDescription() { return description; }
    BigDecimal getAmount() { return amount; }
    boolean isAlternative() { return alternative; }
}
