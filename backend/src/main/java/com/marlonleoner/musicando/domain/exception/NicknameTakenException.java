package com.marlonleoner.musicando.domain.exception;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class NicknameTakenException extends BaseException {

    private static final Integer CODE = HttpStatus.CONFLICT.value();

    public NicknameTakenException(String nickname) {
        super(String.format("Nickname %s is already in use in this room", nickname), CODE, ErrorCode.NICKNAME_TAKEN);
    }
}
