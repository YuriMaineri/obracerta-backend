package br.com.obracerta.catalog;

import br.com.obracerta.catalog.internal.MaterialPriceRepository;
import br.com.obracerta.catalog.internal.MaterialRepository;
import br.com.obracerta.catalog.internal.MaterialUsage;
import br.com.obracerta.catalog.internal.ServiceRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** API do módulo de catálogo: insumos com preço e serviços com composição. */
@org.springframework.stereotype.Service
@Transactional(readOnly = true)
public class CatalogService {

    private final MaterialRepository materials;
    private final MaterialPriceRepository prices;
    private final ServiceRepository services;

    CatalogService(MaterialRepository materials, MaterialPriceRepository prices, ServiceRepository services) {
        this.materials = materials;
        this.prices = prices;
        this.services = services;
    }

    public List<Material> listMaterials(String query) {
        return materials.findByActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(term(query));
    }

    public Material findMaterial(Long id) {
        return materials.findById(id).orElseThrow(() -> new NoSuchElementException("Insumo não encontrado: " + id));
    }

    public Map<Long, List<String>> materialUsage() {
        return services.findActiveMaterialUsage().stream().collect(Collectors.groupingBy(MaterialUsage::materialId,
                LinkedHashMap::new, Collectors.mapping(MaterialUsage::serviceName, Collectors.toList())));
    }

    public List<MaterialPrice> priceHistory(Long materialId) {
        findMaterial(materialId);
        return prices.findByMaterialIdOrderByEffectiveDateDescIdDesc(materialId);
    }

    @Transactional
    public Material createMaterial(MaterialRequest request) {
        Material material = materials.save(new Material(request.name().trim(), request.unit()));
        applyMaterial(material, request);
        return material;
    }

    @Transactional
    public Material updateMaterial(Long id, MaterialRequest request) {
        Material material = findMaterial(id);
        applyMaterial(material, request);
        return material;
    }

    @Transactional
    public void deactivateMaterial(Long id) {
        Material material = findMaterial(id);
        List<String> usedBy = materialUsage().getOrDefault(id, List.of());
        if (!usedBy.isEmpty()) {
            throw new IllegalArgumentException("O insumo \"%s\" está na composição de: %s. Tire-o desses serviços antes de remover."
                    .formatted(material.getName(), String.join(", ", usedBy)));
        }
        material.deactivate();
    }

    public List<ServiceDetails> listServices(String query, ServiceCategory category) {
        List<Service> found = category == null
                ? services.findByActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(term(query))
                : services.findByActiveTrueAndCategoryAndNameContainingIgnoreCaseOrderByNameAsc(category, term(query));
        return found.stream().map(ServiceDetails::of).toList();
    }

    public ServiceDetails findService(Long id) {
        return ServiceDetails.of(loadService(id));
    }

    @Transactional
    public ServiceDetails createService(ServiceRequest request) {
        Service service = new Service(request.name().trim());
        applyService(service, request);
        return ServiceDetails.of(services.save(service));
    }

    @Transactional
    public ServiceDetails updateService(Long id, ServiceRequest request) {
        Service service = loadService(id);
        applyService(service, request);
        return ServiceDetails.of(service);
    }

    @Transactional
    public void deactivateService(Long id) {
        loadService(id).deactivate();
    }

    private Service loadService(Long id) {
        return services.findById(id).orElseThrow(() -> new NoSuchElementException("Serviço não encontrado: " + id));
    }

    private void applyMaterial(Material material, MaterialRequest request) {
        material.rename(request.name().trim(), request.unit(), blankToNull(request.supplier()));
        LocalDate today = LocalDate.now();
        if (material.changePrice(request.price(), today)) {
            prices.save(new MaterialPrice(material.getId(), request.price(), today));
        }
    }

    private void applyService(Service service, ServiceRequest request) {
        BigDecimal days = request.daysPerUnit() == null ? null : request.daysPerUnit().setScale(4, RoundingMode.HALF_UP);
        service.update(request.name().trim(), request.category(), request.unit(),
                blankToNull(request.proposalDescription()), days, request.usualPrice());
        service.replaceComposition(compositionEntries(service, request.composition()));
    }

    private List<Service.CompositionEntry> compositionEntries(Service service, List<ServiceRequest.CompositionLine> lines) {
        if (lines == null) {
            return List.of();
        }
        Set<Long> alreadyUsed = service.getComposition().stream()
                .map(c -> c.getMaterial().getId()).collect(Collectors.toSet());
        Set<Long> seen = new HashSet<>();
        List<Service.CompositionEntry> entries = new ArrayList<>();
        for (ServiceRequest.CompositionLine line : lines) {
            Material material = findMaterial(line.materialId());
            if (!seen.add(material.getId())) {
                throw new IllegalArgumentException("O insumo \"%s\" aparece mais de uma vez na composição."
                        .formatted(material.getName()));
            }
            if (!material.isActive() && !alreadyUsed.contains(material.getId())) {
                throw new IllegalArgumentException("O insumo \"%s\" foi removido do catálogo.".formatted(material.getName()));
            }
            BigDecimal waste = line.wastePercent() == null ? BigDecimal.ZERO : line.wastePercent();
            entries.add(new Service.CompositionEntry(material, line.quantityPerUnit(), waste));
        }
        return entries;
    }

    private static String term(String query) {
        return query == null ? "" : query.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
