package com.marlonleoner.musicando.dto.response;

import java.util.List;

/**
 * Fim da rodada: resposta certa, os mais rápidos que acertaram, o resultado de cada jogador e o placar.
 */
public record RoundEndedBroadcast(int round, int totalRounds, String answer, String artist, String cover, boolean lastRound, long durationMs,
                                  List<RoundResultResponse> fastest, List<RoundResultResponse> results, List<ScoreResponse> scoreboard) {
    public RoundEndedBroadcast {
        fastest = List.copyOf(fastest);
        results = List.copyOf(results);
        scoreboard = List.copyOf(scoreboard);
    }
}
