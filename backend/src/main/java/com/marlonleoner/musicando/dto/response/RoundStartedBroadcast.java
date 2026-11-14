package com.marlonleoner.musicando.dto.response;

import java.util.List;

/**
 * Música da rodada tocando: opções para os jogadores e quem já respondeu ({@code durationMs} é o tempo que falta).
 */
public record RoundStartedBroadcast(int round, int totalRounds, List<String> options, long durationMs, long roundDurationMs, List<String> answeredPlayerIds) {
    public RoundStartedBroadcast {
        options = List.copyOf(options);
        answeredPlayerIds = List.copyOf(answeredPlayerIds);
    }
}
