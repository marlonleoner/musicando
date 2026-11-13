package com.marlonleoner.musicando.domain.exception;

import com.marlonleoner.musicando.domain.socket.SocketErrorCode;
import lombok.Getter;

/**
 * Comando recusado (tipo desconhecido ou papel sem permissão). O erro é devolvido ao remetente em
 * {@code /user/queue/errors}, sem encerrar a conexão.
 */
@Getter
public class SocketCommandException extends RuntimeException {

    private final SocketErrorCode errorCode;

    public SocketCommandException(SocketErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
