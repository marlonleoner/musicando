package com.marlonleoner.musicando.domain.exception;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidAvatarException extends BaseException {

    private static final Integer CODE = HttpStatus.BAD_REQUEST.value();

    public InvalidAvatarException(String avatar) {
        super(String.format("Avatar %s does not exist", avatar), CODE, ErrorCode.INVALID_AVATAR);
    }
}
