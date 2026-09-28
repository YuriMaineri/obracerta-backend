package br.com.obracerta.company;

import jakarta.persistence.*;

/** Texto reutilizável da proposta. As marcadas como padrão já vêm selecionadas no orçamento novo. */
@Entity
@Table(name = "clause")
public class Clause {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClauseType type;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "is_default", nullable = false)
    private boolean defaultClause;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean active = true;

    protected Clause() {
    }

    Clause(Long companyId) {
        this.companyId = companyId;
    }

    void update(ClauseRequest request) {
        this.type = request.type();
        this.title = request.title().trim();
        this.content = request.content().trim();
        this.defaultClause = request.defaultClause();
        this.sortOrder = request.sortOrder() == null ? 0 : request.sortOrder();
    }

    void deactivate() {
        this.active = false;
    }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public ClauseType getType() { return type; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public boolean isDefaultClause() { return defaultClause; }
    public int getSortOrder() { return sortOrder; }
    public boolean isActive() { return active; }
}
