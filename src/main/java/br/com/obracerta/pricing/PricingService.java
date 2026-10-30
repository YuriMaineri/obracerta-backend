package br.com.obracerta.pricing;

import br.com.obracerta.pricing.internal.PricingSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** API do módulo de preço. A configuração é criada pela migration V8. */
@Service
@Transactional(readOnly = true)
public class PricingService {

    private final PricingSettingsRepository repository;

    PricingService(PricingSettingsRepository repository) {
        this.repository = repository;
    }

    public PricingSettings getSettings() {
        return repository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Configuração de preço não encontrada"));
    }

    public Rates rates() {
        return getSettings().toRates();
    }

    @Transactional
    public PricingSettings updateSettings(PricingSettingsRequest request) {
        PricingSettings settings = getSettings();
        settings.update(request);
        return settings;
    }
}
