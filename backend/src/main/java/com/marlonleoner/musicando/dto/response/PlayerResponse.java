package com.marlonleoner.musicando.dto.response;

/**
 * Dados públicos de um jogador, sem informações sensíveis.
 */
public record PlayerResponse(String id, String nickname, int score, boolean owner, boolean connected) {
}
