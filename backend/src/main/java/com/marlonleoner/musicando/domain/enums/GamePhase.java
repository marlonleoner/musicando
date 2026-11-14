package com.marlonleoner.musicando.domain.enums;

/**
 * Etapa da rodada atual de uma partida em andamento.
 */
public enum GamePhase {

    /** Contagem regressiva antes da música tocar. */
    COUNTDOWN,

    /** Música tocando e jogadores respondendo. */
    PLAYING,

    /** Resposta certa e placar da rodada sendo exibidos. */
    REVEAL,

    /** Todas as rodadas foram jogadas. */
    FINISHED
}
