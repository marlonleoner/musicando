package com.marlonleoner.musicando.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Regras configuráveis das salas.
 *
 * @param maxPlayers quantidade máxima de jogadores por sala ({@code musicando.room.max-players}, variável de ambiente
 *                   {@code ROOM_MAX_PLAYERS}); 12 por padrão
 * @param minPlayers quantidade mínima de jogadores conectados para iniciar a partida ({@code musicando.room.min-players},
 *                   variável de ambiente {@code ROOM_MIN_PLAYERS}); 1 por padrão
 */
@ConfigurationProperties(prefix = "musicando.room")
public record RoomProperties(@DefaultValue("12") int maxPlayers, @DefaultValue("1") int minPlayers) {
    public RoomProperties {
        if (maxPlayers < 1) {
            throw new IllegalArgumentException("musicando.room.max-players must be at least 1");
        }

        if (minPlayers < 1 || minPlayers > maxPlayers) {
            throw new IllegalArgumentException("musicando.room.min-players must be between 1 and musicando.room.max-players");
        }
    }
}
