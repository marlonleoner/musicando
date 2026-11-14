package com.marlonleoner.musicando.domain.enums;

/**
 * Códigos de erro enviados aos clientes, tanto na API REST quanto no WebSocket.
 */
public enum ErrorCode {

    INVALID_CREDENTIALS,
    FORBIDDEN,
    ROOM_NOT_FOUND,
    PLAYER_NOT_FOUND,
    ROOM_NOT_JOINABLE,
    ROOM_FULL,
    INVALID_NICKNAME,
    NICKNAME_TAKEN,
    INVALID_COMMAND,
    INTERNAL_ERROR
}
