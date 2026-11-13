package com.marlonleoner.musicando.dto.socket;

import com.marlonleoner.musicando.domain.socket.RoomStateReason;

public record SocketEventReason(RoomStateReason type, String participantId) {
}
