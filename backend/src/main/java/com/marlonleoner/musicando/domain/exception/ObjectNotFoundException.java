package com.marlonleoner.musicando.domain.exception;

import org.springframework.http.HttpStatus;

import com.marlonleoner.musicando.domain.BaseEntity;

public class ObjectNotFoundException extends BaseException {

    private static final Integer CODE = HttpStatus.NOT_FOUND.value();

    public ObjectNotFoundException(String message) {
        super(message, CODE);
    }

    public ObjectNotFoundException(Class<? extends BaseEntity> type) {
        super(type.getSimpleName() + " not found", CODE);
    }
}