package com.marlonleoner.musicando.domain.music;

/**
 * Resposta de um jogador na rodada atual.
 *
 * @param option         índice da opção escolhida
 * @param responseTimeMs tempo entre o início da música e a resposta
 * @param correct        se acertou o título
 * @param points         pontos ganhos na rodada
 */
public record PlayerAnswer(int option, long responseTimeMs, boolean correct, int points) {
}
