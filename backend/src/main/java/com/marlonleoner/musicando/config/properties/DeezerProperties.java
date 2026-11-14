package com.marlonleoner.musicando.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Integração com a Deezer.
 *
 * @param baseUrl       URL da API ({@code musicando.deezer.base-url})
 * @param timeout       tempo máximo de cada requisição
 * @param featuredLimit quantidade de playlists em destaque oferecidas ao VIP
 * @param maxTracks     quantidade máxima de músicas lidas de uma playlist
 */
@ConfigurationProperties(prefix = "musicando.deezer")
public record DeezerProperties(
        @DefaultValue("https://api.deezer.com") String baseUrl,
        @DefaultValue("5s") Duration timeout,
        @DefaultValue("25") int featuredLimit,
        @DefaultValue("300") int maxTracks) {
}
