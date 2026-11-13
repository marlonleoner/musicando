package com.marlonleoner.musicando.domain.exception;

import com.marlonleoner.musicando.domain.enums.RoomStatus;
import org.springframework.http.HttpStatus;

public class RoomNotJoinableException extends BaseException {

    private static final Integer CODE = HttpStatus.CONFLICT.value();

    public RoomNotJoinableException(String code, RoomStatus status) {
        super(String.format("Room %s is not accepting new players (status: %s)", code, status), CODE);
    }
}
