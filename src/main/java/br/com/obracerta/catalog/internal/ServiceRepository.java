package br.com.obracerta.catalog.internal;

import br.com.obracerta.catalog.Service;
import br.com.obracerta.catalog.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findByActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(String name);

    List<Service> findByActiveTrueAndCategoryAndNameContainingIgnoreCaseOrderByNameAsc(
            ServiceCategory category, String name);

    @Query("""
            select new br.com.obracerta.catalog.internal.MaterialUsage(c.material.id, s.name)
            from Service s join s.composition c
            where s.active = true
            order by s.name""")
    List<MaterialUsage> findActiveMaterialUsage();
}
