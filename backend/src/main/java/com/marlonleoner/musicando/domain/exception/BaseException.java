package com.marlonleoner.musicando.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseException extends Exception {

    private final Integer code;

    public BaseException(String message) {
        super(message);
        this.code = HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    public BaseException(String message, Integer code) {
        super(message);
        this.code = code;
    }
}
