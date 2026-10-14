package br.com.obracerta.catalog;

import br.com.obracerta.shared.Unit;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Insumo comprado pela empresa (tinta, cimento, cabo...), com o preço atual. */
@Entity
@Table(name = "material")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Unit unit;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(length = 150)
    private String supplier;

    private LocalDate updatedOn;

    @Column(nullable = false)
    private boolean active = true;

    protected Material() {
    }

    Material(String name, Unit unit) {
        this.name = name;
        this.unit = unit;
    }

    void rename(String name, Unit unit, String supplier) {
        this.name = name;
        this.unit = unit;
        this.supplier = supplier;
    }

    /** Devolve true quando o preço mudou, para o histórico registrar o valor novo. */
    boolean changePrice(BigDecimal newPrice, LocalDate today) {
        boolean changed = newPrice == null ? price != null : price == null || newPrice.compareTo(price) != 0;
        if (changed) {
            this.price = newPrice;
            this.updatedOn = today;
        }
        return changed && newPrice != null;
    }

    void deactivate() {
        this.active = false;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Unit getUnit() { return unit; }
    public BigDecimal getPrice() { return price; }
    public String getSupplier() { return supplier; }
    public LocalDate getUpdatedOn() { return updatedOn; }
    public boolean isActive() { return active; }
}
