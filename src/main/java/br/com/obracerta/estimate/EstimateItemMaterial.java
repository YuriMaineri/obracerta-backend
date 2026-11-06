package br.com.obracerta.estimate;

import br.com.obracerta.shared.Unit;
import jakarta.persistence.*;

import java.math.BigDecimal;

/** Insumo de um serviço do orçamento, copiado do catálogo ao salvar: é a memória de cálculo do item. */
@Entity
@Table(name = "estimate_item_material")
class EstimateItemMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id")
    private EstimateItem item;

    private Long materialId;

    @Column(nullable = false, length = 200)
    private String materialName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Unit unit;

    @Column(length = 30)
    private String packageName;

    @Column(precision = 10, scale = 3)
    private BigDecimal packageSize;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal quantityPerUnit;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal wastePercent;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal quantity;

    @Column(precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal cost;

    @Column(nullable = false)
    private int sortOrder;

    protected EstimateItemMaterial() {
    }

    EstimateItemMaterial(EstimateItem item, EstimateDetails.MaterialUse use, int sortOrder) {
        this.item = item;
        this.materialId = use.materialId();
        this.materialName = use.material();
        this.unit = use.unit();
        this.packageName = use.packageName();
        this.packageSize = use.packageSize();
        this.quantityPerUnit = use.quantityPerUnit();
        this.wastePercent = use.wastePercent();
        this.quantity = use.quantity();
        this.unitPrice = use.unitPrice();
        this.cost = use.cost();
        this.sortOrder = sortOrder;
    }

    EstimateDetails.MaterialUse toUse() {
        return new EstimateDetails.MaterialUse(materialId, materialName, unit, packageName, packageSize,
                quantityPerUnit, wastePercent, quantity, unitPrice, cost);
    }
}
