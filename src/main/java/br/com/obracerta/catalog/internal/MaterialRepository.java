package br.com.obracerta.catalog.internal;

import br.com.obracerta.catalog.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findByActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(String name);
}
