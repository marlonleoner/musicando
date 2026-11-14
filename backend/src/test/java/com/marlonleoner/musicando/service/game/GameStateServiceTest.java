package com.marlonleoner.musicando.service.game;

import com.marlonleoner.musicando.domain.music.GameRound;
import com.marlonleoner.musicando.domain.music.GameState;
import com.marlonleoner.musicando.service.cache.RedisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameStateServiceTest {

    private static final String CODE = "ABC123";

    private static final String KEY = "@MUSICANDO:GAMES:ABC123";

    @Mock
    private RedisService redisService;

    @InjectMocks
    private GameStateService service;

    private final GameState state = new GameState("match-id", 20, List.of(new GameRound("1", "A", List.of("A", "B", "C", "D"))), 0);

    @Test
    void shouldStoreStateWithTtlWhenSaving() {
        service.save(CODE, state);

        verify(redisService).set(KEY, state, 6 * 60 * 60);
    }

    @Test
    void shouldReturnStoredStateWhenFinding() {
        when(redisService.get(KEY, GameState.class)).thenReturn(Optional.of(state));

        assertThat(service.find(CODE)).contains(state);
    }

    @Test
    void shouldInvalidateKeyWhenDeleting() {
        service.delete(CODE);

        verify(redisService).invalidate(KEY);
    }
}
