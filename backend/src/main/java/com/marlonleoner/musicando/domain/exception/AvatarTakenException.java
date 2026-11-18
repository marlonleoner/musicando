package com.marlonleoner.musicando.domain.exception;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class AvatarTakenException extends BaseException {

    private static final Integer CODE = HttpStatus.CONFLICT.value();

    public AvatarTakenException(String avatar) {
        super(String.format("Avatar %s is already in use in this room", avatar), CODE, ErrorCode.AVATAR_TAKEN);
    }
}
