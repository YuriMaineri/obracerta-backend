package br.com.obracerta.catalog;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Linha da composição: quanto de um insumo o serviço consome por unidade, mais a perda. */
@Entity
@Table(name = "service_material")
public class ServiceMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id")
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id")
    private Material material;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal quantityPerUnit;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal wastePercent;

    @Column(nullable = false)
    private int sortOrder;

    protected ServiceMaterial() {
    }

    ServiceMaterial(Service service, Material material) {
        this.service = service;
        this.material = material;
    }

    void update(BigDecimal quantityPerUnit, BigDecimal wastePercent, int sortOrder) {
        this.quantityPerUnit = quantityPerUnit;
        this.wastePercent = wastePercent;
        this.sortOrder = sortOrder;
    }

    public Material getMaterial() { return material; }
    public BigDecimal getQuantityPerUnit() { return quantityPerUnit; }
    public BigDecimal getWastePercent() { return wastePercent; }
    public int getSortOrder() { return sortOrder; }
}
