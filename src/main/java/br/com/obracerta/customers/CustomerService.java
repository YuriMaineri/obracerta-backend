package br.com.obracerta.customers;

import br.com.obracerta.customers.internal.CustomerRepository;
import br.com.obracerta.shared.BrazilianDocuments;
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
        applyContactAndAddress(customer, request);
        return repository.save(customer);
    }

    @Transactional
    public Customer update(Long id, CustomerRequest request) {
        Customer customer = findById(id);
        customer.updateIdentification(request.personType(), request.name().trim(),
                normalizeTaxId(request.personType(), request.taxId()));
        applyContactAndAddress(customer, request);
        return customer;
    }

    @Transactional
    public void deactivate(Long id) {
        findById(id).deactivate();
    }

    private static void applyContactAndAddress(Customer customer, CustomerRequest request) {
        customer.updateContact(blankToNull(request.contactPerson()), normalizePhone(request.phone()),
                request.email() == null || request.email().isBlank() ? null : request.email().trim().toLowerCase());
        customer.updateAddress(blankToNull(request.address()), blankToNull(request.district()),
                blankToNull(request.city()), normalizePostalCode(request.postalCode()));
    }

    /**
     * Guarda so os digitos do CPF/CNPJ e confere tamanho e digitos verificadores
     * conforme o tipo de pessoa. Documento e opcional: alguns orcamentos reais para
     * pessoa fisica nao trazem CPF. A mensagem fica em portugues porque e exibida ao usuario.
     */
    static String normalizeTaxId(Customer.PersonType type, String taxId) {
        String digits = BrazilianDocuments.digitsOnly(taxId);
        if (digits.isEmpty()) {
            return null;
        }
        boolean individual = type == Customer.PersonType.INDIVIDUAL;
        String label = individual ? "CPF" : "CNPJ";
        int expectedLength = individual ? 11 : 14;
        if (digits.length() != expectedLength) {
            throw new IllegalArgumentException("%s deve ter %d digitos, recebido: %s"
                    .formatted(label, expectedLength, taxId));
        }
        boolean valid = individual ? BrazilianDocuments.isValidCpf(digits) : BrazilianDocuments.isValidCnpj(digits);
        if (!valid) {
            throw new IllegalArgumentException("%s invalido: %s".formatted(label, taxId));
        }
        return digits;
    }

    /** Telefone com DDD: 10 digitos (fixo) ou 11 (celular). Guardado so com digitos. */
    static String normalizePhone(String phone) {
        String digits = BrazilianDocuments.digitsOnly(phone);
        if (digits.isEmpty()) {
            return null;
        }
        if (digits.length() != 10 && digits.length() != 11) {
            throw new IllegalArgumentException("Telefone deve ter DDD e 8 ou 9 digitos, recebido: " + phone);
        }
        return digits;
    }

    /** CEP com 8 digitos. Guardado so com digitos. */
    static String normalizePostalCode(String postalCode) {
        String digits = BrazilianDocuments.digitsOnly(postalCode);
        if (digits.isEmpty()) {
            return null;
        }
        if (digits.length() != 8) {
            throw new IllegalArgumentException("CEP deve ter 8 digitos, recebido: " + postalCode);
        }
        return digits;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
