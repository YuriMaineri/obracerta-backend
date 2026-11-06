package br.com.obracerta.identity;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bloqueia o e-mail por 15 minutos depois de 5 senhas erradas seguidas. Fica em memória: a API roda numa instância só,
 * e reiniciar a API libera os bloqueios.
 */
@Component
class LoginThrottle {

    static final int MAX_FAILURES = 5;
    static final Duration LOCK = Duration.ofMinutes(15);
    private static final int CLEANUP_THRESHOLD = 10_000;

    private record Failures(int count, Instant last, Instant lockedUntil) {
    }

    private final Map<String, Failures> failures = new ConcurrentHashMap<>();
    private final Clock clock;

    LoginThrottle() {
        this(Clock.systemUTC());
    }

    LoginThrottle(Clock clock) {
        this.clock = clock;
    }

    /** Até quando o e-mail está bloqueado; nulo quando pode tentar. */
    Instant lockedUntil(String email) {
        Failures f = failures.get(key(email));
        return f != null && f.lockedUntil() != null && f.lockedUntil().isAfter(clock.instant()) ? f.lockedUntil() : null;
    }

    /** Conta uma senha errada; erros mais antigos que o bloqueio não contam. */
    void failed(String email) {
        Instant now = clock.instant();
        if (failures.size() > CLEANUP_THRESHOLD) {
            failures.values().removeIf(f -> f.last().plus(LOCK).isBefore(now));
        }
        failures.compute(key(email), (k, f) -> {
            int count = f == null || f.last().plus(LOCK).isBefore(now) ? 1 : f.count() + 1;
            return new Failures(count, now, count >= MAX_FAILURES ? now.plus(LOCK) : null);
        });
    }

    void succeeded(String email) {
        failures.remove(key(email));
    }

    private static String key(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
