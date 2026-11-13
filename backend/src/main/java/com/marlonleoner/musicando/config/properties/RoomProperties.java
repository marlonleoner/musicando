package com.marlonleoner.musicando.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Regras configuráveis das salas.
 *
 * @param maxPlayers quantidade máxima de jogadores por sala ({@code musicando.room.max-players}, variável de ambiente
 *                   {@code ROOM_MAX_PLAYERS}); 12 por padrão
 */
@ConfigurationProperties(prefix = "musicando.room")
public record RoomProperties(@DefaultValue("12") int maxPlayers) {
    public RoomProperties {
        if (maxPlayers < 1) {
            throw new IllegalArgumentException("musicando.room.max-players must be at least 1");
        }
    }
}
