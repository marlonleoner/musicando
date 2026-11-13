package com.marlonleoner.musicando.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * Origens autorizadas a acessar a API REST e o endpoint WebSocket.
 *
 * @param allowedOrigins lista de origens (ex.: {@code http://localhost:5173}); vazia por padrão, bloqueando
 *                       qualquer origem externa
 */
@ConfigurationProperties(prefix = "musicando.cors")
public record CorsProperties(@DefaultValue List<String> allowedOrigins) {
    public CorsProperties {
        allowedOrigins = List.copyOf(allowedOrigins);
    }

    public String[] allowedOriginsAsArray() {
        return this.allowedOrigins.toArray(String[]::new);
    }
}
