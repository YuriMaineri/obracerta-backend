package br.com.obracerta.customers;

import br.com.obracerta.customers.internal.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

/**
 * API do modulo de clientes. E por aqui que os demais modulos conversam com este.
 */
@Service
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository repository;

    CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public Page<Customer> search(String query, Pageable pageable) {
        String term = query == null ? "" : query.trim();
        return repository.findByActiveTrueAndNameContainingIgnoreCase(term, pageable);
    }

    public Customer findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cliente nao encontrado: " + id));
    }

    @Transactional
    public Customer create(CustomerRequest request) {
        Customer customer = new Customer(request.personType(), request.name().trim(),
                normalizeTaxId(request.personType(), request.taxId()));
        customer.updateContact(request.contactPerson(), request.phone(), request.email());
        customer.updateAddress(request.address(), request.district(), request.city(), request.postalCode());
        return repository.save(customer);
    }

    @Transactional
    public Customer update(Long id, CustomerRequest request) {
        Customer customer = findById(id);
        customer.updateIdentification(request.personType(), request.name().trim(),
                normalizeTaxId(request.personType(), request.taxId()));
        customer.updateContact(request.contactPerson(), request.phone(), request.email());
        customer.updateAddress(request.address(), request.district(), request.city(), request.postalCode());
        return customer;
    }

    @Transactional
    public void deactivate(Long id) {
        findById(id).deactivate();
    }

    /**
     * Guarda so os digitos do CPF/CNPJ e confere o tamanho conforme o tipo de pessoa.
     * Documento e opcional: alguns orcamentos reais para pessoa fisica nao trazem CPF.
     * A mensagem de erro fica em portugues porque e exibida ao usuario.
     */
    static String normalizeTaxId(Customer.PersonType type, String taxId) {
        if (taxId == null || taxId.isBlank()) {
            return null;
        }
        String digits = taxId.replaceAll("\\D", "");
        boolean individual = type == Customer.PersonType.INDIVIDUAL;
        int expectedLength = individual ? 11 : 14;
        if (digits.length() != expectedLength) {
            throw new IllegalArgumentException("%s deve ter %d digitos, recebido: %s"
                    .formatted(individual ? "CPF" : "CNPJ", expectedLength, taxId));
        }
        return digits;
    }
}
