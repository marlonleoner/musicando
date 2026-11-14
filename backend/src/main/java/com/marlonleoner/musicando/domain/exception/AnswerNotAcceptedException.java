package com.marlonleoner.musicando.domain.exception;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Resposta recusada: fora do tempo da rodada, de outra rodada ou repetida.
 */
public class AnswerNotAcceptedException extends BaseException {

    private static final Integer CODE = HttpStatus.CONFLICT.value();

    public AnswerNotAcceptedException(String message) {
        super(message, CODE, ErrorCode.ANSWER_NOT_ACCEPTED);
    }
}
