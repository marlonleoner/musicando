package com.marlonleoner.musicando.domain.exception;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class RoomFullException extends BaseException {

    private static final Integer CODE = HttpStatus.CONFLICT.value();

    public RoomFullException(String code, int maxPlayers) {
        super(String.format("Room %s is full (maximum of %d players)", code, maxPlayers), CODE, ErrorCode.ROOM_FULL);
    }
}
