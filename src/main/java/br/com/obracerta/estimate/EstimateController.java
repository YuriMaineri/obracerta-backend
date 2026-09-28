package br.com.obracerta.estimate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/estimates")
class EstimateController {

    private final EstimateService service;

    EstimateController(EstimateService service) {
        this.service = service;
    }

    @GetMapping
    Page<EstimateSummary> search(@RequestParam(required = false) String query,
                                 @RequestParam(required = false) EstimateStatus status,
                                 @RequestParam(required = false) Long customerId,
                                 Pageable pageable) {
        return service.search(query, status, customerId, pageable);
    }

    @GetMapping("/{id}")
    EstimateDetails findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    ResponseEntity<EstimateDetails> create(@RequestBody @Valid EstimateRequest request, UriComponentsBuilder uriBuilder) {
        EstimateDetails created = service.create(request);
        return ResponseEntity.created(uriBuilder.path("/api/estimates/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @PutMapping("/{id}")
    EstimateDetails update(@PathVariable Long id, @RequestBody @Valid EstimateRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/duplicate")
    ResponseEntity<EstimateDetails> duplicate(@PathVariable Long id, UriComponentsBuilder uriBuilder) {
        EstimateDetails copy = service.duplicate(id);
        return ResponseEntity.created(uriBuilder.path("/api/estimates/{id}").buildAndExpand(copy.id()).toUri())
                .body(copy);
    }

    @PutMapping("/{id}/status")
    EstimateDetails changeStatus(@PathVariable Long id, @RequestBody @Valid StatusRequest request) {
        return service.changeStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    record StatusRequest(@NotNull(message = "Status e obrigatorio") EstimateStatus status) {
    }
}
