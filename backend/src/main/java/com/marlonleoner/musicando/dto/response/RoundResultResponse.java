package com.marlonleoner.musicando.dto.response;

/**
 * Resultado de um jogador na rodada.
 *
 * @param option índice da opção que o jogador marcou (nulo se não respondeu), para a TV mostrar quem marcou o quê
 */
public record RoundResultResponse(String playerId, String nickname, boolean answered, boolean correct, int points, Long responseTimeMs,
                                  Integer option) {
}
