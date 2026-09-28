package br.com.obracerta.estimate;

import jakarta.persistence.*;

import java.time.Instant;

/** Recado só para o prestador. Existe para não vazar mais nada em vermelho no documento do cliente. */
@Entity
@Table(name = "internal_note")
class InternalNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estimate_id")
    private Estimate estimate;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected InternalNote() {
    }

    InternalNote(Estimate estimate, String content) {
        this.estimate = estimate;
        this.content = content;
    }

    String getContent() { return content; }
}
