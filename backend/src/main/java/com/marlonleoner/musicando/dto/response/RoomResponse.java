package com.marlonleoner.musicando.dto.response;

import com.marlonleoner.musicando.domain.enums.RoomStatus;

/**
 * Dados públicos de uma sala, sem informações sensíveis.
 */
public record RoomResponse(String code, RoomStatus status, boolean connected) {
}
