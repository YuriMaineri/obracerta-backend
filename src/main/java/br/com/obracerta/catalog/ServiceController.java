package br.com.obracerta.catalog;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/services")
class ServiceController {

    private final CatalogService service;

    ServiceController(CatalogService service) {
        this.service = service;
    }

    @GetMapping
    List<ServiceDetails> list(@RequestParam(required = false) String query,
                              @RequestParam(required = false) ServiceCategory category) {
        return service.listServices(query, category);
    }

    @GetMapping("/{id}")
    ServiceDetails findById(@PathVariable Long id) {
        return service.findService(id);
    }

    @PostMapping
    ResponseEntity<ServiceDetails> create(@RequestBody @Valid ServiceRequest request, UriComponentsBuilder uriBuilder) {
        ServiceDetails created = service.createService(request);
        var location = uriBuilder.path("/api/catalog/services/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    ServiceDetails update(@PathVariable Long id, @RequestBody @Valid ServiceRequest request) {
        return service.updateService(id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.deactivateService(id);
        return ResponseEntity.noContent().build();
    }
}
