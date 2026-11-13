package com.marlonleoner.musicando.dto.socket;

import com.marlonleoner.musicando.domain.socket.SocketErrorCode;

public record SocketErrorPayload(SocketErrorCode code, String message, String command) {
}
