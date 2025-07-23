package com.marlonleoner.musicando.domain.exception;

import org.springframework.http.HttpStatus;

public class RoomAlreadyFinishedException extends BaseException {

    private static final Integer CODE = HttpStatus.CONFLICT.value();

    public RoomAlreadyFinishedException(String code) {
        super(String.format("Room %s already been finalized", code), CODE);
    }
}