package com.marlonleoner.musicando.domain.exception;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class NotRoomOwnerException extends BaseException {

    private static final Integer CODE = HttpStatus.FORBIDDEN.value();

    public NotRoomOwnerException() {
        super("Only the VIP can perform this action", CODE, ErrorCode.FORBIDDEN);
    }
}
