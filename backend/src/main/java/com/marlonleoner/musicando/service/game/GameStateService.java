package com.marlonleoner.musicando.service.game;

import com.marlonleoner.musicando.domain.music.GameState;
import com.marlonleoner.musicando.service.cache.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Estado das partidas em andamento, guardado no Redis.
 */
@Service
@RequiredArgsConstructor
public class GameStateService {

    private static final String KEY = "@MUSICANDO:GAMES:%s";

    private static final int TTL_SECONDS = 6 * 60 * 60;

    private final RedisService redisService;

    public void save(String roomCode, GameState state) {
        redisService.set(key(roomCode), state, TTL_SECONDS);
    }

    public Optional<GameState> find(String roomCode) {
        return redisService.get(key(roomCode), GameState.class);
    }

    public void delete(String roomCode) {
        redisService.invalidate(key(roomCode));
    }

    private String key(String roomCode) {
        return String.format(KEY, roomCode);
    }
}
