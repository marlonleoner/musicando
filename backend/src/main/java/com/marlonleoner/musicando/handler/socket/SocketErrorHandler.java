package com.marlonleoner.musicando.handler.socket;

import com.marlonleoner.musicando.domain.exception.SocketAuthenticationException;
import com.marlonleoner.musicando.domain.socket.SocketErrorCode;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.util.Objects;

/**
 * Converte falhas no processamento de frames do cliente em frames {@code ERROR}, com o código do erro no header
 * {@code message}. Detalhes internos nunca são enviados ao cliente.
 */
@Component
public class SocketErrorHandler extends StompSubProtocolErrorHandler {

    private static final byte[] EMPTY_PAYLOAD = new byte[0];

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setMessage(resolveCode(ex).name());
        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(EMPTY_PAYLOAD, accessor.getMessageHeaders());
    }

    private SocketErrorCode resolveCode(Throwable ex) {
        Throwable current = ex;

        while (Objects.nonNull(current)) {
            if (current instanceof SocketAuthenticationException authenticationException) {
                return authenticationException.getErrorCode();
            }

            current = current.getCause();
        }

        return SocketErrorCode.INTERNAL_ERROR;
    }
}
