package com.marlonleoner.musicando.dto.response;

import com.marlonleoner.musicando.domain.enums.ErrorCode;

public record ErrorResponse(Integer status, ErrorCode code, String message) {
}
