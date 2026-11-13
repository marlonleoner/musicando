package com.marlonleoner.musicando.handler.socket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocketSessionRegistryTest {

    private final SocketSessionRegistry registry = new SocketSessionRegistry();

    @Mock
    private WebSocketHandler delegate;

    @Mock
    private WebSocketSession session;

    @Test
    void shouldCloseRegisteredSessionWhenSessionIsReplaced() throws Exception {
        when(session.getId()).thenReturn("session-1");
        registry.decorate(delegate).afterConnectionEstablished(session);

        registry.closeReplaced("session-1");

        verify(delegate).afterConnectionEstablished(session);
        verify(session).close(SocketSessionRegistry.SESSION_REPLACED);
    }

    @Test
    void shouldNotCloseSessionWhenItWasAlreadyClosed() throws Exception {
        when(session.getId()).thenReturn("session-1");
        final WebSocketHandler decorated = registry.decorate(delegate);
        decorated.afterConnectionEstablished(session);
        decorated.afterConnectionClosed(session, CloseStatus.NORMAL);

        registry.closeReplaced("session-1");

        verify(delegate).afterConnectionClosed(session, CloseStatus.NORMAL);
        verify(session, never()).close(SocketSessionRegistry.SESSION_REPLACED);
    }

    @Test
    void shouldIgnoreUnknownSessionWhenClosingReplacedSession() {
        assertThatCode(() -> registry.closeReplaced("unknown")).doesNotThrowAnyException();
    }

    @Test
    void shouldNotPropagateErrorWhenClosingSessionFails() throws Exception {
        when(session.getId()).thenReturn("session-1");
        doThrow(new IOException("broken pipe")).when(session).close(SocketSessionRegistry.SESSION_REPLACED);
        registry.decorate(delegate).afterConnectionEstablished(session);

        assertThatCode(() -> registry.closeReplaced("session-1")).doesNotThrowAnyException();
    }
}
