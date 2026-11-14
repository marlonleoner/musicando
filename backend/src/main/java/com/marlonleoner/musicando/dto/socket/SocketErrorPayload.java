package com.marlonleoner.musicando.dto.socket;

import com.marlonleoner.musicando.domain.enums.ErrorCode;

public record SocketErrorPayload(ErrorCode code, String message, String command) {
}
