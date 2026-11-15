package com.marlonleoner.musicando.domain.exception;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Limite de requisições excedido; o cliente pode tentar de novo em instantes.
 */
public class TooManyRequestsException extends BaseException {

    private static final Integer CODE = HttpStatus.TOO_MANY_REQUESTS.value();

    public TooManyRequestsException() {
        super("Too many requests, try again in a moment", CODE, ErrorCode.RATE_LIMITED);
    }
}
