package br.com.obracerta.catalog;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Preço de um insumo a partir de uma data. Guarda a evolução para reorçar obras antigas. */
@Entity
@Table(name = "material_price_history")
public class MaterialPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long materialId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    protected MaterialPrice() {
    }

    MaterialPrice(Long materialId, BigDecimal price, LocalDate effectiveDate) {
        this.materialId = materialId;
        this.price = price;
        this.effectiveDate = effectiveDate;
    }

    public Long getMaterialId() { return materialId; }
    public BigDecimal getPrice() { return price; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
}
