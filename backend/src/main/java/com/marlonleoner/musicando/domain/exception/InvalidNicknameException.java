package com.marlonleoner.musicando.domain.exception;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidNicknameException extends BaseException {

    private static final Integer CODE = HttpStatus.BAD_REQUEST.value();

    public InvalidNicknameException(String message) {
        super(message, CODE, ErrorCode.INVALID_NICKNAME);
    }
}
