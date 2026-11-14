package com.marlonleoner.musicando.service.game;

import com.marlonleoner.musicando.domain.enums.GamePhase;
import com.marlonleoner.musicando.domain.music.GameRound;
import com.marlonleoner.musicando.domain.music.GameState;
import com.marlonleoner.musicando.domain.music.PlayerAnswer;
import com.marlonleoner.musicando.service.cache.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameStateServiceTest {

    private static final String CODE = "ABC123";

    private static final String KEY = "@MUSICANDO:GAMES:ABC123";

    @Mock
    private RedisService redisService;

    private GameStateService service;

    @BeforeEach
    void setUp() {
        service = new GameStateService(redisService, JsonMapper.builder().build());
    }

    private static GameState state() {
        return GameState.builder()
                .matchId("match-id")
                .roundDuration(20)
                .rounds(new ArrayList<>(List.of(new GameRound("1", "A", List.of("A", "B", "C", "D")))))
                .phase(GamePhase.PLAYING)
                .phaseStartedAt(1000L)
                .phaseEndsAt(21000L)
                .pausedRemainingMs(5000L)
                .previewUrl("preview.mp3")
                .answers(new HashMap<>(Map.of("player-1", new PlayerAnswer(0, 1500, true, 963))))
                .scores(new HashMap<>(Map.of("player-1", 963)))
                .roundsWon(new HashMap<>(Map.of("player-1", 1)))
                .build();
    }

    @Test
    void shouldStoreStateAsJsonWithTtlAndReadItBackWhenSavingAndFinding() {
        service.save(CODE, state());

        final ArgumentCaptor<Object> json = ArgumentCaptor.forClass(Object.class);
        verify(redisService).set(eq(KEY), json.capture(), eq(6 * 60 * 60));
        when(redisService.get(KEY, String.class)).thenReturn(Optional.of((String) json.getValue()));

        final GameState found = service.find(CODE).orElseThrow();

        assertThat(found).usingRecursiveComparison().isEqualTo(state());
        assertThat(found.paused()).isTrue();
        assertThat(found.round().answer()).isEqualTo("A");
    }

    @Test
    void shouldReturnEmptyWhenThereIsNoState() {
        when(redisService.get(KEY, String.class)).thenReturn(Optional.empty());

        assertThat(service.find(CODE)).isEmpty();
    }

    @Test
    void shouldInvalidateKeyWhenDeleting() {
        service.delete(CODE);

        verify(redisService).invalidate(KEY);
    }
}
