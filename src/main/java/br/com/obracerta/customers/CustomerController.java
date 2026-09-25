package br.com.obracerta.customers;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/customers")
class CustomerController {

    private final CustomerService service;

    CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    Page<CustomerResponse> search(@RequestParam(required = false) String query, Pageable pageable) {
        return service.search(query, pageable).map(CustomerResponse::from);
    }

    @GetMapping("/{id}")
    CustomerResponse findById(@PathVariable Long id) {
        return CustomerResponse.from(service.findById(id));
    }

    @PostMapping
    ResponseEntity<CustomerResponse> create(@RequestBody @Valid CustomerRequest request,
                                            UriComponentsBuilder uriBuilder) {
        Customer customer = service.create(request);
        var location = uriBuilder.path("/api/customers/{id}").buildAndExpand(customer.getId()).toUri();
        return ResponseEntity.created(location).body(CustomerResponse.from(customer));
    }

    @PutMapping("/{id}")
    CustomerResponse update(@PathVariable Long id, @RequestBody @Valid CustomerRequest request) {
        return CustomerResponse.from(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    /** Resposta completa: a tela de edicao precisa de todos os campos. */
    record CustomerResponse(Long id, String personType, String name, String taxId,
                            String contactPerson, String phone, String email,
                            String address, String district, String city, String postalCode) {

        static CustomerResponse from(Customer c) {
            return new CustomerResponse(c.getId(), c.getPersonType().name(), c.getName(),
                    c.getTaxId(), c.getContactPerson(), c.getPhone(), c.getEmail(),
                    c.getAddress(), c.getDistrict(), c.getCity(), c.getPostalCode());
        }
    }
}
