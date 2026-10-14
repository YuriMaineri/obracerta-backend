package br.com.obracerta.catalog;

import br.com.obracerta.shared.PricingMode;
import br.com.obracerta.shared.Unit;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Serviço do catálogo com a composição própria do prestador. */
@Entity
@Table(name = "service")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 80)
    private ServiceCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Unit unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PricingMode pricingMode;

    @Column(columnDefinition = "text")
    private String proposalDescription;

    @Column(precision = 10, scale = 4)
    private BigDecimal daysPerUnit;

    @Column(precision = 12, scale = 2)
    private BigDecimal usualPrice;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceMaterial> composition = new ArrayList<>();

    protected Service() {
    }

    Service(String name) {
        this.name = name;
    }

    void update(String name, ServiceCategory category, Unit unit, String proposalDescription,
                BigDecimal daysPerUnit, BigDecimal usualPrice) {
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.pricingMode = pricingModeFor(unit);
        this.proposalDescription = proposalDescription;
        this.daysPerUnit = daysPerUnit;
        this.usualPrice = usualPrice;
    }

    /** Atualiza as linhas no lugar (pelo insumo), para não violar a unicidade serviço + insumo no banco. */
    void replaceComposition(List<CompositionEntry> entries) {
        Map<Long, ServiceMaterial> current = composition.stream()
                .collect(Collectors.toMap(c -> c.getMaterial().getId(), Function.identity()));
        List<ServiceMaterial> next = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            CompositionEntry entry = entries.get(i);
            ServiceMaterial line = current.get(entry.material().getId());
            if (line == null) {
                line = new ServiceMaterial(this, entry.material());
            }
            line.update(entry.quantityPerUnit(), entry.wastePercent(), i);
            next.add(line);
        }
        composition.removeIf(line -> !next.contains(line));
        next.stream().filter(line -> !composition.contains(line)).forEach(composition::add);
        composition.sort(Comparator.comparingInt(ServiceMaterial::getSortOrder));
    }

    void deactivate() {
        this.active = false;
    }

    /** A unidade define como a quantidade chega ao orçamento: medida, peças ou verba. */
    static PricingMode pricingModeFor(Unit unit) {
        return switch (unit) {
            case SQUARE_METER, METER, CUBIC_METER -> PricingMode.BY_MEASUREMENT;
            case PIECE, POINT -> PricingMode.PER_PIECE;
            case LUMP_SUM -> PricingMode.LUMP_SUM;
            default -> throw new IllegalArgumentException(
                    "Serviço é cobrado por m², m, m³, unidade, ponto ou verba, não por " + unit.getSymbol() + ".");
        };
    }

    record CompositionEntry(Material material, BigDecimal quantityPerUnit, BigDecimal wastePercent) {
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public ServiceCategory getCategory() { return category; }
    public Unit getUnit() { return unit; }
    public PricingMode getPricingMode() { return pricingMode; }
    public String getProposalDescription() { return proposalDescription; }
    public BigDecimal getDaysPerUnit() { return daysPerUnit; }
    public BigDecimal getUsualPrice() { return usualPrice; }
    public boolean isActive() { return active; }

    public List<ServiceMaterial> getComposition() {
        return composition.stream().sorted(Comparator.comparingInt(ServiceMaterial::getSortOrder)).toList();
    }
}
