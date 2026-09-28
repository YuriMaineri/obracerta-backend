package br.com.obracerta.estimate;

import br.com.obracerta.shared.MaterialSupply;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Orçamento. Na Fatia 1 guarda exatamente o que hoje vai no documento, em texto livre e sem cálculo. */
@Entity
@Table(name = "estimate")
public class Estimate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30, updatable = false)
    private String number;

    @Column(nullable = false)
    private Long customerId;

    @Column(length = 200)
    private String title;

    private Long bankAccountId;

    @Column(nullable = false)
    private LocalDate issueDate;

    private Integer validityDays;

    private Integer minLeadDays;

    private Integer maxLeadDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MaterialSupply materialSupply = MaterialSupply.ITEMIZED;

    @Column(precision = 12, scale = 2)
    private BigDecimal estimatedMaterialCost;

    @Column(columnDefinition = "text")
    private String paymentTerms;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstimateStatus status = EstimateStatus.DRAFT;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "estimate", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder")
    private List<EstimateSection> sections = new ArrayList<>();

    @OneToMany(mappedBy = "estimate", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder")
    private List<EstimateMaterial> materials = new ArrayList<>();

    @OneToMany(mappedBy = "estimate", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder")
    private List<EstimatePriceLine> priceLines = new ArrayList<>();

    @OneToMany(mappedBy = "estimate", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder")
    private List<Exclusion> exclusions = new ArrayList<>();

    @OneToMany(mappedBy = "estimate", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<InternalNote> internalNotes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "estimate_clause", joinColumns = @JoinColumn(name = "estimate_id"))
    @Column(name = "clause_id")
    private Set<Long> clauseIds = new HashSet<>();

    protected Estimate() {
    }

    Estimate(String number, Long customerId) {
        this.number = number;
        this.customerId = customerId;
        EstimateSection section = new EstimateSection(this, "Serviços");
        section.getAreas().add(new EstimateArea(section, "Geral"));
        this.sections.add(section);
    }

    void updateHeader(EstimateRequest r, Long bankAccountId) {
        this.customerId = r.customerId();
        this.title = blankToNull(r.title());
        this.bankAccountId = bankAccountId;
        this.issueDate = r.issueDate() == null ? LocalDate.now() : r.issueDate();
        this.validityDays = r.validityDays();
        this.minLeadDays = r.minLeadDays();
        this.maxLeadDays = r.maxLeadDays();
        this.materialSupply = r.materialSupply();
        this.estimatedMaterialCost = r.estimatedMaterialCost();
        this.paymentTerms = blankToNull(r.paymentTerms());
        this.updatedAt = Instant.now();
    }

    void replaceServices(List<EstimateRequest.ServiceLine> lines) {
        EstimateArea area = defaultArea();
        area.getItems().clear();
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            area.getItems().add(new EstimateItem(area, line.description().trim(), blankToNull(line.internalNote()), i));
        }
    }

    void replaceMaterials(List<EstimateRequest.MaterialLine> lines) {
        materials.clear();
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            materials.add(new EstimateMaterial(this, line.description().trim(), line.quantity(), line.totalPrice(), i));
        }
    }

    void replacePriceLines(List<EstimateRequest.PriceLine> lines) {
        priceLines.clear();
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            priceLines.add(new EstimatePriceLine(this, line.description().trim(), line.amount(), i));
        }
    }

    void replaceExclusions(List<String> contents) {
        exclusions.clear();
        for (int i = 0; i < contents.size(); i++) {
            exclusions.add(new Exclusion(this, contents.get(i).trim(), i));
        }
    }

    void replaceInternalNotes(List<String> contents) {
        internalNotes.clear();
        contents.forEach(content -> internalNotes.add(new InternalNote(this, content.trim())));
    }

    void replaceClauses(Set<Long> ids) {
        clauseIds.clear();
        clauseIds.addAll(ids);
    }

    void changeStatus(EstimateStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    List<EstimateItem> services() {
        return defaultArea().getItems();
    }

    private EstimateArea defaultArea() {
        return sections.getFirst().getAreas().getFirst();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public Long getId() { return id; }
    public String getNumber() { return number; }
    public Long getCustomerId() { return customerId; }
    public String getTitle() { return title; }
    public Long getBankAccountId() { return bankAccountId; }
    public LocalDate getIssueDate() { return issueDate; }
    public Integer getValidityDays() { return validityDays; }
    public Integer getMinLeadDays() { return minLeadDays; }
    public Integer getMaxLeadDays() { return maxLeadDays; }
    public MaterialSupply getMaterialSupply() { return materialSupply; }
    public BigDecimal getEstimatedMaterialCost() { return estimatedMaterialCost; }
    public String getPaymentTerms() { return paymentTerms; }
    public EstimateStatus getStatus() { return status; }
    public Instant getUpdatedAt() { return updatedAt; }
    List<EstimateMaterial> getMaterials() { return materials; }
    List<EstimatePriceLine> getPriceLines() { return priceLines; }
    List<Exclusion> getExclusions() { return exclusions; }
    List<InternalNote> getInternalNotes() { return internalNotes; }
    Set<Long> getClauseIds() { return clauseIds; }
}
