package br.com.obracerta.estimate;

import jakarta.persistence.*;

/** Observação específica da obra, como "Não incluso neste orçamento os ventiladores". */
@Entity
@Table(name = "exclusion")
class Exclusion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estimate_id")
    private Estimate estimate;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false)
    private int sortOrder;

    protected Exclusion() {
    }

    Exclusion(Estimate estimate, String content, int sortOrder) {
        this.estimate = estimate;
        this.content = content;
        this.sortOrder = sortOrder;
    }

    String getContent() { return content; }
}
