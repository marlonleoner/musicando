package com.marlonleoner.musicando.dto.response;

/**
 * Resultado de um jogador na rodada.
 */
public record RoundResultResponse(String playerId, String nickname, boolean answered, boolean correct, int points, Long responseTimeMs) {
}
