package br.com.obracerta.estimate;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/** Ambiente (sala, banheiro, fachada). Na Fatia 1 os serviços ficam num ambiente "Geral". */
@Entity
@Table(name = "area")
class EstimateArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id")
    private EstimateSection section;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private int sortOrder;

    @OneToMany(mappedBy = "area", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder")
    private List<EstimateItem> items = new ArrayList<>();

    protected EstimateArea() {
    }

    EstimateArea(EstimateSection section, String name) {
        this.section = section;
        this.name = name;
    }

    List<EstimateItem> getItems() { return items; }
}
