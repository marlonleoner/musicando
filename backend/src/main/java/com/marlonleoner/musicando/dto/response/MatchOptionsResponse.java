package com.marlonleoner.musicando.dto.response;

import java.util.List;
import java.util.Objects;

/**
 * Valores aceitos na configuração da partida, para o VIP escolher.
 */
public record MatchOptionsResponse(List<Integer> rounds, List<Integer> roundDurations, int defaultRounds, int defaultRoundDuration) {
    public MatchOptionsResponse {
        rounds = Objects.isNull(rounds) ? List.of() : List.copyOf(rounds);
        roundDurations = Objects.isNull(roundDurations) ? List.of() : List.copyOf(roundDurations);
    }
}
