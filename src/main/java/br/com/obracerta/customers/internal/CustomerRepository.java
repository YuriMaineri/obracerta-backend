package br.com.obracerta.customers.internal;

import br.com.obracerta.customers.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Detalhe interno do modulo. Esta em customers.internal justamente para que
 * nenhum outro modulo consiga injetar o repositorio: quem precisa de cliente
 * passa por CustomerService.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
}
