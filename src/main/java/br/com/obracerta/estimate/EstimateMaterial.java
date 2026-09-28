package br.com.obracerta.estimate;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Linha de "MATERIAL A SER USADO", com ou sem preço conforme o regime de material. */
@Entity
@Table(name = "estimate_material")
class EstimateMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estimate_id")
    private Estimate estimate;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false, length = 20)
    private String source = "MANUAL";

    @Column(nullable = false)
    private int sortOrder;

    protected EstimateMaterial() {
    }

    EstimateMaterial(Estimate estimate, String description, BigDecimal quantity, BigDecimal totalPrice, int sortOrder) {
        this.estimate = estimate;
        this.description = description;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.sortOrder = sortOrder;
    }

    String getDescription() { return description; }
    BigDecimal getQuantity() { return quantity; }
    BigDecimal getTotalPrice() { return totalPrice; }
}
