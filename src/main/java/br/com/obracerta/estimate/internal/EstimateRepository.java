package br.com.obracerta.estimate.internal;

import br.com.obracerta.estimate.Estimate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EstimateRepository extends JpaRepository<Estimate, Long>, JpaSpecificationExecutor<Estimate> {

    @Query("select max(e.number) from Estimate e where e.number like concat(:prefix, '%')")
    String findLastNumberWithPrefix(@Param("prefix") String prefix);
}
