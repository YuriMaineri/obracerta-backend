package br.com.obracerta.catalog.internal;

import br.com.obracerta.catalog.MaterialPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialPriceRepository extends JpaRepository<MaterialPrice, Long> {

    List<MaterialPrice> findByMaterialIdOrderByEffectiveDateDescIdDesc(Long materialId);
}
