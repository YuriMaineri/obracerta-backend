package br.com.obracerta.estimate;

import br.com.obracerta.shared.PricingMode;
import jakarta.persistence.*;

/** Linha de "SERVIÇO A SER EXECUTADO". A nota interna nunca vai para a proposta do cliente. */
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

    protected EstimateItem() {
    }

    EstimateItem(EstimateArea area, String description, String internalNote, int sortOrder) {
        this.area = area;
        this.description = description;
        this.internalNote = internalNote;
        this.sortOrder = sortOrder;
    }

    String getDescription() { return description; }
    String getInternalNote() { return internalNote; }
}
