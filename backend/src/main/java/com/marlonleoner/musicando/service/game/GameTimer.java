package com.marlonleoner.musicando.service.game;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Agenda a próxima transição de cada sala. Cada sala tem no máximo uma transição pendente: agendar uma nova cancela
 * a anterior.
 */
@Component
public class GameTimer implements DisposableBean {

    private static final int POOL_SIZE = 4;

    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

    public GameTimer() {
        scheduler.setPoolSize(POOL_SIZE);
        scheduler.setThreadNamePrefix("game-timer-");
        scheduler.initialize();
    }

    public void schedule(String roomCode, long delayMs, Runnable task) {
        cancel(roomCode);
        tasks.put(roomCode, scheduler.schedule(task, Instant.now().plusMillis(Math.max(0, delayMs))));
    }

    public void cancel(String roomCode) {
        final ScheduledFuture<?> task = tasks.remove(roomCode);

        if (Objects.nonNull(task)) {
            task.cancel(false);
        }
    }

    @Override
    public void destroy() {
        scheduler.shutdown();
    }
}
