package com.marlonleoner.musicando.domain.socket;

/**
 * Motivo do envio de um {@code ROOM_STATE}. Serve apenas para a interface exibir avisos; o estado em si é sempre a
 * foto completa da sala.
 */
public enum RoomStateReason {

    SNAPSHOT,
    HOST_CONNECTED,
    HOST_DISCONNECTED,
    PLAYER_JOINED,
    PLAYER_CONNECTED,
    PLAYER_DISCONNECTED,
    PLAYER_LEFT,
    PLAYER_KICKED,
    ROOM_CLOSED
}
