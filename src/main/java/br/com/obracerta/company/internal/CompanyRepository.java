package br.com.obracerta.company.internal;

import br.com.obracerta.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findFirstByOrderByIdAsc();
}
