package br.com.obracerta.estimate;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/** Bloco do orçamento. Na Fatia 1 existe um único bloco obrigatório; alternativas entram na Fatia 4. */
@Entity
@Table(name = "estimate_section")
class EstimateSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estimate_id")
    private Estimate estimate;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 20)
    private String type = "REQUIRED";

    @Column(nullable = false)
    private int sortOrder;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder")
    private List<EstimateArea> areas = new ArrayList<>();

    protected EstimateSection() {
    }

    EstimateSection(Estimate estimate, String title) {
        this.estimate = estimate;
        this.title = title;
    }

    List<EstimateArea> getAreas() { return areas; }
}
