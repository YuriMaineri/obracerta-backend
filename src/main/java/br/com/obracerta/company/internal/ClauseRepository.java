package br.com.obracerta.company.internal;

import br.com.obracerta.company.Clause;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClauseRepository extends JpaRepository<Clause, Long> {

    List<Clause> findByCompanyIdAndActiveTrueOrderByTypeAscSortOrderAscTitleAsc(Long companyId);

    Optional<Clause> findByIdAndCompanyIdAndActiveTrue(Long id, Long companyId);
}
