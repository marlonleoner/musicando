package com.marlonleoner.musicando.dto.response;

/**
 * Prévia da música da rodada, enviada somente à TV.
 *
 * @param round         número da rodada (a partir de 1)
 * @param previewUrl    link da prévia, ou nulo quando a Deezer não devolveu prévia
 * @param offsetMs      quanto da rodada já passou (a música já deveria ter tocado esse tanto)
 * @param startFraction ponto de início sorteado para a rodada, de 0 a 1
 * @param playMs        quanto a música toca: a rodada mais a revelação. A TV começa em
 *                      {@code startFraction × (duração da prévia − playMs)}, nunca antes do começo
 */
public record RoundAudioBroadcast(int round, String previewUrl, long offsetMs, double startFraction, long playMs) {
}
