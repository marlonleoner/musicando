package com.marlonleoner.musicando.dto.response;

/**
 * Contagem regressiva antes da música da rodada ({@code durationMs} é o tempo que falta).
 */
public record RoundCountdownBroadcast(int round, int totalRounds, long durationMs) {
}
