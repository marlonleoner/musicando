package com.marlonleoner.musicando.service.game;

import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.InvalidSettingsException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameLocksTest {

    private final GameLocks locks = new GameLocks();

    @Test
    void shouldRunActionAndPropagateExceptionWhenActionFails() {
        assertThatThrownBy(() -> locks.run("ABC123", () -> {
            throw new InvalidSettingsException("boom");
        })).isInstanceOf(InvalidSettingsException.class);

        final List<String> calls = new ArrayList<>();
        locks.execute("ABC123", () -> calls.add("ran"));

        assertThat(calls).containsExactly("ran");
    }

    @Test
    void shouldAllowReentranceWhenSameThreadAlreadyHoldsTheLock() throws BaseException {
        final List<String> calls = new ArrayList<>();

        locks.run("ABC123", () -> locks.execute("ABC123", () -> calls.add("inner")));

        assertThat(calls).containsExactly("inner");
    }

    @Test
    void shouldSerializeActionsWhenTheyRunConcurrentlyInTheSameRoom() throws InterruptedException {
        final AtomicInteger inside = new AtomicInteger();
        final AtomicInteger maxInside = new AtomicInteger();
        final int threads = 8;
        final CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            Thread.ofVirtual().start(() -> {
                locks.execute("ABC123", () -> {
                    maxInside.accumulateAndGet(inside.incrementAndGet(), Math::max);
                    Thread.onSpinWait();
                    inside.decrementAndGet();
                });
                done.countDown();
            });
        }

        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(maxInside.get()).isEqualTo(1);
        locks.release("ABC123");
    }
}
