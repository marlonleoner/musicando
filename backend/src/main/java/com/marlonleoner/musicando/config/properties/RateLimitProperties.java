package com.marlonleoner.musicando.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Limites de uso, controlados em memória (uma instância do backend).
 *
 * @param roomCreationsPerMinute salas criadas por minuto, por IP
 * @param roomJoinsPerMinute     tentativas de entrar em sala por minuto, por IP
 * @param commandsPerSecond      comandos enviados pelo socket por segundo, por participante
 * @param musicSearchesPerMinute buscas de playlist e links resolvidos por minuto, por IP
 */
@ConfigurationProperties(prefix = "musicando.rate-limit")
public record RateLimitProperties(
        @DefaultValue("10") int roomCreationsPerMinute,
        @DefaultValue("20") int roomJoinsPerMinute,
        @DefaultValue("20") int commandsPerSecond,
        @DefaultValue("60") int musicSearchesPerMinute) {
}
