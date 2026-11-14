package com.marlonleoner.musicando.dto.response;

/**
 * Um jogador respondeu (sem revelar a opção escolhida).
 */
public record AnswerReceivedBroadcast(int round, String playerId, int answeredCount, int totalPlayers) {
}
