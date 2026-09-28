package br.com.obracerta.company;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/company/clauses")
class ClauseController {

    private final CompanyService service;

    ClauseController(CompanyService service) {
        this.service = service;
    }

    @GetMapping
    List<ClauseResponse> list() {
        return service.listClauses().stream().map(ClauseResponse::from).toList();
    }

    @PostMapping
    ResponseEntity<ClauseResponse> create(@RequestBody @Valid ClauseRequest request, UriComponentsBuilder uriBuilder) {
        Clause clause = service.createClause(request);
        var location = uriBuilder.path("/api/company/clauses/{id}").buildAndExpand(clause.getId()).toUri();
        return ResponseEntity.created(location).body(ClauseResponse.from(clause));
    }

    @PutMapping("/{id}")
    ClauseResponse update(@PathVariable Long id, @RequestBody @Valid ClauseRequest request) {
        return ClauseResponse.from(service.updateClause(id, request));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.deactivateClause(id);
        return ResponseEntity.noContent().build();
    }

    record ClauseResponse(Long id, ClauseType type, String title, String content, boolean defaultClause, int sortOrder) {

        static ClauseResponse from(Clause c) {
            return new ClauseResponse(c.getId(), c.getType(), c.getTitle(), c.getContent(),
                    c.isDefaultClause(), c.getSortOrder());
        }
    }
}
