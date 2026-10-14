package br.com.obracerta.catalog;

import br.com.obracerta.shared.Unit;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalog/materials")
class MaterialController {

    private final CatalogService service;

    MaterialController(CatalogService service) {
        this.service = service;
    }

    @GetMapping
    List<MaterialResponse> list(@RequestParam(required = false) String query) {
        Map<Long, List<String>> usage = service.materialUsage();
        return service.listMaterials(query).stream()
                .map(m -> MaterialResponse.from(m, usage.getOrDefault(m.getId(), List.of())))
                .toList();
    }

    @GetMapping("/{id}/prices")
    List<PriceResponse> prices(@PathVariable Long id) {
        return service.priceHistory(id).stream().map(p -> new PriceResponse(p.getPrice(), p.getEffectiveDate())).toList();
    }

    @PostMapping
    ResponseEntity<MaterialResponse> create(@RequestBody @Valid MaterialRequest request, UriComponentsBuilder uriBuilder) {
        Material material = service.createMaterial(request);
        var location = uriBuilder.path("/api/catalog/materials/{id}").buildAndExpand(material.getId()).toUri();
        return ResponseEntity.created(location).body(MaterialResponse.from(material, List.of()));
    }

    @PutMapping("/{id}")
    MaterialResponse update(@PathVariable Long id, @RequestBody @Valid MaterialRequest request) {
        Material material = service.updateMaterial(id, request);
        return MaterialResponse.from(material, service.materialUsage().getOrDefault(id, List.of()));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.deactivateMaterial(id);
        return ResponseEntity.noContent().build();
    }

    record MaterialResponse(Long id, String name, Unit unit, BigDecimal price, String supplier,
                            LocalDate updatedOn, List<String> usedBy) {

        static MaterialResponse from(Material m, List<String> usedBy) {
            return new MaterialResponse(m.getId(), m.getName(), m.getUnit(), m.getPrice(), m.getSupplier(),
                    m.getUpdatedOn(), usedBy);
        }
    }

    record PriceResponse(BigDecimal price, LocalDate effectiveDate) {
    }
}
