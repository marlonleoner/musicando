package com.marlonleoner.musicando.dto.response;

/**
 * Prévia da música da rodada, enviada somente à TV. {@code offsetMs} é quanto da música já deveria ter tocado.
 */
public record RoundAudioBroadcast(int round, String previewUrl, long offsetMs) {
}
