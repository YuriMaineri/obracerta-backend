package br.com.obracerta.identity;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class LoginThrottleTest {

    private final MovableClock clock = new MovableClock();
    private final LoginThrottle throttle = new LoginThrottle(clock);

    @Test
    void locksAfterFiveFailuresAndReleasesAfterFifteenMinutes() {
        failTimes(4);
        assertThat(throttle.lockedUntil("carlos@maineri.com.br")).isNull();

        throttle.failed("carlos@maineri.com.br");
        assertThat(throttle.lockedUntil(" CARLOS@maineri.com.br ")).isEqualTo(clock.instant().plus(LoginThrottle.LOCK));

        clock.advance(LoginThrottle.LOCK.plusSeconds(1));
        assertThat(throttle.lockedUntil("carlos@maineri.com.br")).isNull();
        throttle.failed("carlos@maineri.com.br");
        assertThat(throttle.lockedUntil("carlos@maineri.com.br")).isNull();
    }

    @Test
    void successAndOldFailuresResetTheCount() {
        failTimes(4);
        throttle.succeeded("carlos@maineri.com.br");
        failTimes(4);
        assertThat(throttle.lockedUntil("carlos@maineri.com.br")).isNull();

        clock.advance(Duration.ofMinutes(16));
        failTimes(4);
        assertThat(throttle.lockedUntil("carlos@maineri.com.br")).isNull();
    }

    private void failTimes(int times) {
        for (int i = 0; i < times; i++) {
            throttle.failed("carlos@maineri.com.br");
        }
    }

    private static final class MovableClock extends Clock {

        private Instant now = Instant.parse("2026-11-06T12:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
