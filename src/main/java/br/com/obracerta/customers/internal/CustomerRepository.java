package br.com.obracerta.customers.internal;

import br.com.obracerta.customers.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Customer> findByActiveTrueAndPersonTypeAndNameContainingIgnoreCase(
            Customer.PersonType personType, String name, Pageable pageable);
}
