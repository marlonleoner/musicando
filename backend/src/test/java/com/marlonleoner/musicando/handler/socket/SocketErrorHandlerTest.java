package com.marlonleoner.musicando.handler.socket;

import com.marlonleoner.musicando.domain.exception.SocketAuthenticationException;
import com.marlonleoner.musicando.domain.socket.SocketErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import static org.assertj.core.api.Assertions.assertThat;

class SocketErrorHandlerTest {

    private final SocketErrorHandler handler = new SocketErrorHandler();

    @Test
    void shouldSendErrorCodeWhenAuthenticationFails() {
        final Message<byte[]> error = handler.handleClientMessageProcessingError(null, new SocketAuthenticationException(SocketErrorCode.INVALID_CREDENTIALS));

        assertErrorFrame(error, "INVALID_CREDENTIALS");
    }

    @Test
    void shouldFindErrorCodeWhenAuthenticationExceptionIsWrapped() {
        final MessagingException wrapped = new MessagingException("delivery failed", new SocketAuthenticationException(SocketErrorCode.FORBIDDEN));

        assertErrorFrame(handler.handleClientMessageProcessingError(null, wrapped), "FORBIDDEN");
    }

    @Test
    void shouldHideDetailsWhenErrorIsUnexpected() {
        final Message<byte[]> error = handler.handleClientMessageProcessingError(null, new IllegalStateException("database password is 123"));

        assertErrorFrame(error, "INTERNAL_ERROR");
        assertThat(error.getPayload()).isEmpty();
    }

    private void assertErrorFrame(final Message<byte[]> error, final String code) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(error);
        assertThat(accessor.getCommand()).isEqualTo(StompCommand.ERROR);
        assertThat(accessor.getMessage()).isEqualTo(code);
    }
}
