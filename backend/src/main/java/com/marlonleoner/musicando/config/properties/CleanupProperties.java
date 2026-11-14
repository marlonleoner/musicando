package com.marlonleoner.musicando.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Limpeza automática de salas.
 *
 * @param abandonedAfter  tempo sem a TV conectada para a sala ser encerrada
 * @param closedRetention tempo que uma sala encerrada fica no banco antes de ser apagada
 */
@ConfigurationProperties(prefix = "musicando.cleanup")
public record CleanupProperties(
        @DefaultValue("30m") Duration abandonedAfter,
        @DefaultValue("24h") Duration closedRetention) {
}
