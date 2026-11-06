package br.com.obracerta.estimate;

import br.com.obracerta.shared.Difficulty;
import br.com.obracerta.shared.PricingMode;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Linha de "SERVIÇO A SER EXECUTADO". Ligada ao catálogo, guarda o cálculo do momento em que foi salva. */
@Entity
@Table(name = "estimate_item")
class EstimateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "area_id")
    private EstimateArea area;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PricingMode pricingMode = PricingMode.LUMP_SUM;

    @Column(columnDefinition = "text")
    private String internalNote;

    @Column(nullable = false)
    private int sortOrder;

    private Long serviceId;

    @Column(precision = 12, scale = 3)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Difficulty difficulty;

    @Column(precision = 10, scale = 3)
    private BigDecimal laborDays;

    @Column(precision = 12, scale = 2)
    private BigDecimal laborCost;

    @Column(precision = 12, scale = 2)
    private BigDecimal materialCost;

    @Column(name = "total_price", precision = 12, scale = 2)
    private BigDecimal suggestedPrice;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder")
    private List<EstimateItemMaterial> materials = new ArrayList<>();

    protected EstimateItem() {
    }

    EstimateItem(EstimateArea area, ServiceEntry entry, int sortOrder) {
        this.area = area;
        this.description = entry.description();
        this.internalNote = entry.internalNote();
        this.sortOrder = sortOrder;
        if (entry.serviceId() != null) {
            this.serviceId = entry.serviceId();
            this.pricingMode = entry.pricingMode();
            this.quantity = entry.quantity();
            this.difficulty = entry.difficulty();
            this.laborDays = entry.quote().laborDays();
            this.laborCost = entry.quote().laborCost();
            this.materialCost = entry.quote().materialCost();
            this.suggestedPrice = entry.quote().price();
            for (int i = 0; i < entry.materials().size(); i++) {
                materials.add(new EstimateItemMaterial(this, entry.materials().get(i), i));
            }
        }
    }

    String getDescription() { return description; }
    String getInternalNote() { return internalNote; }
    Long getServiceId() { return serviceId; }
    BigDecimal getQuantity() { return quantity; }
    Difficulty getDifficulty() { return difficulty; }
    BigDecimal getSuggestedPrice() { return suggestedPrice; }
    BigDecimal getLaborDays() { return laborDays; }
    BigDecimal getLaborCost() { return laborCost; }
    BigDecimal getMaterialCost() { return materialCost; }

    List<EstimateDetails.MaterialUse> materialUses() {
        return materials.stream().map(EstimateItemMaterial::toUse).toList();
    }
}
