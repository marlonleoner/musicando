package com.marlonleoner.musicando.domain.exception;

import com.marlonleoner.musicando.domain.BaseEntity;
import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class ObjectNotFoundException extends BaseException {

    private static final Integer CODE = HttpStatus.NOT_FOUND.value();

    public ObjectNotFoundException(Class<? extends BaseEntity> type) {
        super(type.getSimpleName() + " not found", CODE, Player.class.equals(type) ? ErrorCode.PLAYER_NOT_FOUND : ErrorCode.ROOM_NOT_FOUND);
    }
}
