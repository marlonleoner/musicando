package com.marlonleoner.musicando.domain.exception;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidSettingsException extends BaseException {

    private static final Integer CODE = HttpStatus.BAD_REQUEST.value();

    public InvalidSettingsException(String message) {
        super(message, CODE, ErrorCode.INVALID_SETTINGS);
    }
}
