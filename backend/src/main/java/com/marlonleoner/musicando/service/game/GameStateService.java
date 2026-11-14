package com.marlonleoner.musicando.service.game;

import com.marlonleoner.musicando.domain.music.GameState;
import com.marlonleoner.musicando.service.cache.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

/**
 * Estado das partidas em andamento, guardado no Redis como JSON.
 */
@Service
@RequiredArgsConstructor
public class GameStateService {

    private static final String KEY = "@MUSICANDO:GAMES:%s";

    private static final int TTL_SECONDS = 6 * 60 * 60;

    private final RedisService redisService;

    private final JsonMapper jsonMapper;

    public void save(String roomCode, GameState state) {
        redisService.set(key(roomCode), jsonMapper.writeValueAsString(state), TTL_SECONDS);
    }

    public Optional<GameState> find(String roomCode) {
        return redisService.get(key(roomCode), String.class).map(json -> jsonMapper.readValue(json, GameState.class));
    }

    public void delete(String roomCode) {
        redisService.invalidate(key(roomCode));
    }

    private String key(String roomCode) {
        return String.format(KEY, roomCode);
    }
}
