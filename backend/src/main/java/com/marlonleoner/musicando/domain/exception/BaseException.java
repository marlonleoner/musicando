package com.marlonleoner.musicando.domain.exception;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exceção de negócio, com o status HTTP e o código de erro devolvidos ao cliente.
 */
@Getter
public class BaseException extends Exception {

    private final Integer code;

    private final ErrorCode errorCode;

    public BaseException(String message) {
        this(message, HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorCode.INTERNAL_ERROR);
    }

    public BaseException(String message, Integer code, ErrorCode errorCode) {
        super(message);
        this.code = code;
        this.errorCode = errorCode;
    }
}
