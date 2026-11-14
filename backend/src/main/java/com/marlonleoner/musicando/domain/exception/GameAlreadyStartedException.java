package com.marlonleoner.musicando.domain.exception;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class GameAlreadyStartedException extends BaseException {

    private static final Integer CODE = HttpStatus.CONFLICT.value();

    public GameAlreadyStartedException(String code) {
        super(String.format("The game in room %s has already started", code), CODE, ErrorCode.GAME_ALREADY_STARTED);
    }
}
