package br.com.obracerta.pricing;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/api/pricing/settings")
class PricingController {

    private final PricingService service;

    PricingController(PricingService service) {
        this.service = service;
    }

    @GetMapping
    SettingsResponse get() {
        return SettingsResponse.from(service.getSettings());
    }

    @PutMapping
    SettingsResponse update(@RequestBody @Valid PricingSettingsRequest request) {
        return SettingsResponse.from(service.updateSettings(request));
    }

    record SettingsResponse(BigDecimal dailyRate, BigDecimal indirectPercent, BigDecimal marginPercent,
                            BigDecimal easyFactor, BigDecimal normalFactor, BigDecimal hardFactor,
                            BigDecimal veryHardFactor, Instant updatedAt) {

        static SettingsResponse from(PricingSettings s) {
            return new SettingsResponse(s.getDailyRate(), s.getIndirectPercent(), s.getMarginPercent(),
                    s.getEasyFactor(), s.getNormalFactor(), s.getHardFactor(), s.getVeryHardFactor(),
                    s.getUpdatedAt());
        }
    }
}
