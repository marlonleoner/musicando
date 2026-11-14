package com.marlonleoner.musicando.domain.music;

import java.util.List;
import java.util.Objects;

/**
 * Estado de uma partida em andamento, guardado no Redis.
 *
 * @param matchId       partida
 * @param roundDuration duração de cada rodada, em segundos
 * @param rounds        rodadas sorteadas
 * @param currentRound  índice da rodada atual (0 antes da primeira)
 */
public record GameState(String matchId, int roundDuration, List<GameRound> rounds, int currentRound) {
    public GameState {
        rounds = Objects.isNull(rounds) ? List.of() : List.copyOf(rounds);
    }
}
