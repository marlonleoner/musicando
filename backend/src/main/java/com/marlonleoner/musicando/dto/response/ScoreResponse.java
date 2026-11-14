package com.marlonleoner.musicando.dto.response;

/**
 * Pontuação acumulada de um jogador.
 */
public record ScoreResponse(String playerId, String nickname, int score) {
}
