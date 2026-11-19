package com.marlonleoner.musicando.dto.response;

import java.util.List;

/**
 * Fim da rodada: resposta certa, os mais rápidos que acertaram, o resultado de cada jogador (com a opção que marcou), o
 * placar e as opções da rodada (para a TV mostrar quem marcou cada uma, mesmo depois de uma reconexão).
 */
public record RoundEndedBroadcast(int round, int totalRounds, String answer, String artist, String cover, boolean lastRound, long durationMs,
                                  List<RoundResultResponse> fastest, List<RoundResultResponse> results, List<ScoreResponse> scoreboard,
                                  List<String> options) {
    public RoundEndedBroadcast {
        options = List.copyOf(options);
        fastest = List.copyOf(fastest);
        results = List.copyOf(results);
        scoreboard = List.copyOf(scoreboard);
    }
}
