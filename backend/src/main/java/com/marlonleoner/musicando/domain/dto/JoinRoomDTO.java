package com.marlonleoner.musicando.domain.dto;

public record JoinRoomDTO(
        String playerId,
        String secret,
        String roomCode) {
}
