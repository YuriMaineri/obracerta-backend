package br.com.obracerta.pricing.internal;

import br.com.obracerta.pricing.PricingSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PricingSettingsRepository extends JpaRepository<PricingSettings, Long> {

    Optional<PricingSettings> findFirstByOrderByIdAsc();
}
