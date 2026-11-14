package com.marlonleoner.musicando.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Tempos do ciclo das rodadas.
 *
 * @param countdown    contagem regressiva antes de cada música
 * @param reveal       tempo exibindo a resposta certa e o placar da rodada
 * @param fastestLimit quantidade de jogadores mais rápidos exibidos na revelação
 */
@ConfigurationProperties(prefix = "musicando.game")
public record GameProperties(
        @DefaultValue("3s") Duration countdown,
        @DefaultValue("5s") Duration reveal,
        @DefaultValue("5") int fastestLimit) {
}
