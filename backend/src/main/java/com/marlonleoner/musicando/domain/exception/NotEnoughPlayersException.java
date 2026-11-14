package com.marlonleoner.musicando.domain.exception;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class NotEnoughPlayersException extends BaseException {

    private static final Integer CODE = HttpStatus.CONFLICT.value();

    public NotEnoughPlayersException(int minPlayers) {
        super(String.format("At least %d connected player(s) are required to start the game", minPlayers), CODE, ErrorCode.NOT_ENOUGH_PLAYERS);
    }
}
