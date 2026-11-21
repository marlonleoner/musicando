package com.marlonleoner.musicando.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.Objects;

/**
 * Integração com a Deezer.
 *
 * @param baseUrl       URL da API ({@code musicando.deezer.base-url})
 * @param timeout       tempo máximo de cada requisição
 * @param userId        perfil da Deezer cujas playlists públicas são oferecidas ao VIP (obrigatório: {@code DEEZER_USER_ID})
 * @param searchLimit   quantidade de playlists devolvidas por busca
 * @param maxTracks     quantidade máxima de músicas lidas de uma playlist
 */
@ConfigurationProperties(prefix = "musicando.deezer")
public record DeezerProperties(
        @DefaultValue("https://api.deezer.com") String baseUrl,
        @DefaultValue("5s") Duration timeout,
        String userId,
        @DefaultValue("20") int searchLimit,
        @DefaultValue("300") int maxTracks) {
    public DeezerProperties {
        if (Objects.isNull(userId) || userId.isBlank()) {
            throw new IllegalStateException("musicando.deezer.user-id (DEEZER_USER_ID) is required");
        }
    }
}
