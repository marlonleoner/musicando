package com.marlonleoner.musicando.domain.exception;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import lombok.Getter;
import org.springframework.messaging.MessagingException;

/**
 * Falha de autenticação ou autorização em um frame STOMP. A mensagem é o próprio código do erro, enviado ao cliente
 * no frame {@code ERROR}, que encerra a conexão.
 */
@Getter
public class SocketAuthenticationException extends MessagingException {

    private final ErrorCode errorCode;

    public SocketAuthenticationException(ErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }
}
