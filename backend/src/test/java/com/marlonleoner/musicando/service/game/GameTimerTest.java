package com.marlonleoner.musicando.service.game;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class GameTimerTest {

    private final GameTimer timer = new GameTimer();

    @AfterEach
    void tearDown() {
        timer.destroy();
    }

    @Test
    void shouldRunTaskWhenDelayElapses() throws InterruptedException {
        final CountDownLatch ran = new CountDownLatch(1);

        timer.schedule("ABC123", 10, ran::countDown);

        assertThat(ran.await(2, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void shouldReplacePendingTaskWhenSchedulingAgainForTheSameRoom() throws InterruptedException {
        final AtomicInteger first = new AtomicInteger();
        final CountDownLatch second = new CountDownLatch(1);

        timer.schedule("ABC123", 200, first::incrementAndGet);
        timer.schedule("ABC123", 10, second::countDown);

        assertThat(second.await(2, TimeUnit.SECONDS)).isTrue();
        Thread.sleep(300);
        assertThat(first.get()).isZero();
    }

    @Test
    void shouldNotRunTaskWhenCancelled() throws InterruptedException {
        final AtomicInteger calls = new AtomicInteger();

        timer.schedule("ABC123", 50, calls::incrementAndGet);
        timer.cancel("ABC123");
        timer.cancel("UNKNOWN");
        Thread.sleep(200);

        assertThat(calls.get()).isZero();
    }
}
