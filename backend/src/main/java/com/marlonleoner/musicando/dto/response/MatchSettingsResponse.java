package com.marlonleoner.musicando.dto.response;

/**
 * Configuração da partida, visível a todos na sala.
 *
 * @param playlist      playlist escolhida pelo VIP (ausente até ele escolher)
 * @param rounds        quantidade de rodadas
 * @param roundDuration duração de cada rodada, em segundos
 */
public record MatchSettingsResponse(PlaylistResponse playlist, Integer rounds, Integer roundDuration) {
}
