package com.marlonleoner.musicando.service.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contador por janela fixa, em memória: cada chave pode ser usada até {@code limit} vezes por janela. Considera uma
 * única instância do backend.
 */
@Service
@RequiredArgsConstructor
public class RateLimiter {

    private static final long CLEANUP_INTERVAL_MS = 60_000L;

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    private final Clock clock;

    /**
     * Registra um uso da chave.
     *
     * @return {@code false} quando o limite da janela atual já foi atingido
     */
    public boolean tryAcquire(String key, int limit, Duration window) {
        final long now = clock.millis();
        final Window current = windows.compute(key, (ignored, previous) -> Objects.isNull(previous) || previous.expired(now)
                ? new Window(now + window.toMillis(), 1)
                : new Window(previous.expiresAt(), previous.count() + 1));

        return current.count() <= limit;
    }

    /**
     * Descarta as janelas já encerradas, para a memória não crescer com IPs e participantes antigos.
     */
    @Scheduled(fixedDelay = CLEANUP_INTERVAL_MS)
    public void evictExpired() {
        final long now = clock.millis();
        windows.values().removeIf(window -> window.expired(now));
    }

    int size() {
        return windows.size();
    }

    private record Window(long expiresAt, int count) {
        boolean expired(long now) {
            return now >= expiresAt;
        }
    }
}
