package com.marlonleoner.musicando.service.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterTest {

    private static final Duration WINDOW = Duration.ofSeconds(10);

    private final MutableClock clock = new MutableClock();

    private final RateLimiter limiter = new RateLimiter(clock);

    @Test
    void shouldAllowUpToLimitAndRejectAfterWhenWithinWindow() {
        assertThat(limiter.tryAcquire("key", 2, WINDOW)).isTrue();
        assertThat(limiter.tryAcquire("key", 2, WINDOW)).isTrue();
        assertThat(limiter.tryAcquire("key", 2, WINDOW)).isFalse();
    }

    @Test
    void shouldStartNewWindowWhenPreviousOneExpires() {
        limiter.tryAcquire("key", 1, WINDOW);
        assertThat(limiter.tryAcquire("key", 1, WINDOW)).isFalse();

        clock.advance(WINDOW.toMillis());

        assertThat(limiter.tryAcquire("key", 1, WINDOW)).isTrue();
    }

    @Test
    void shouldCountEachKeySeparately() {
        limiter.tryAcquire("first", 1, WINDOW);

        assertThat(limiter.tryAcquire("second", 1, WINDOW)).isTrue();
    }

    @Test
    void shouldEvictOnlyExpiredWindowsWhenCleaningUp() {
        limiter.tryAcquire("old", 1, WINDOW);
        clock.advance(WINDOW.toMillis());
        limiter.tryAcquire("new", 1, WINDOW);

        limiter.evictExpired();

        assertThat(limiter.size()).isEqualTo(1);
        assertThat(limiter.tryAcquire("new", 1, WINDOW)).isFalse();
    }

    private static final class MutableClock extends Clock {

        private long millis;

        void advance(final long delta) {
            millis += delta;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(final ZoneId zone) {
            return this;
        }

        @Override
        public long millis() {
            return millis;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis);
        }
    }
}
