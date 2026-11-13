package com.marlonleoner.musicando.handler.socket;

import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.service.socket.PresenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SocketSessionHandlerTest {

    private static final String SESSION_ID = "session-1";

    private static final SocketPrincipal PLAYER = new SocketPrincipal(RoleEnum.PLAYER, "player-1", "ABC123");

    @Mock
    private PresenceService presenceService;

    @InjectMocks
    private SocketSessionHandler handler;

    @Test
    void shouldRegisterPresenceWhenSessionIsConnected() throws ObjectNotFoundException {
        handler.handleConnected(new SessionConnectedEvent(this, message(StompCommand.CONNECTED, null), PLAYER));

        verify(presenceService).onConnected(PLAYER, SESSION_ID);
    }

    @Test
    void shouldIgnoreConnectedEventWhenPrincipalIsNotAuthenticated() {
        final Principal anonymous = () -> "anonymous";

        handler.handleConnected(new SessionConnectedEvent(this, message(StompCommand.CONNECTED, null), anonymous));

        verifyNoInteractions(presenceService);
    }

    @Test
    void shouldNotPropagateErrorWhenConnectedParticipantDoesNotExist() throws ObjectNotFoundException {
        doThrow(new ObjectNotFoundException(Room.class)).when(presenceService).onConnected(PLAYER, SESSION_ID);
        final SessionConnectedEvent event = new SessionConnectedEvent(this, message(StompCommand.CONNECTED, null), PLAYER);

        assertThatCode(() -> handler.handleConnected(event)).doesNotThrowAnyException();
    }

    @Test
    void shouldRemovePresenceWhenSessionIsDisconnected() throws ObjectNotFoundException {
        handler.handleDisconnect(new SessionDisconnectEvent(this, message(StompCommand.DISCONNECT, null), SESSION_ID, CloseStatus.NORMAL, PLAYER));

        verify(presenceService).onDisconnected(PLAYER, SESSION_ID);
    }

    @Test
    void shouldIgnoreDisconnectWhenSessionWasNeverAuthenticated() {
        handler.handleDisconnect(new SessionDisconnectEvent(this, message(StompCommand.DISCONNECT, null), SESSION_ID, CloseStatus.NORMAL));

        verifyNoInteractions(presenceService);
    }

    @Test
    void shouldNotPropagateErrorWhenDisconnectedParticipantDoesNotExist() throws ObjectNotFoundException {
        doThrow(new ObjectNotFoundException(Room.class)).when(presenceService).onDisconnected(PLAYER, SESSION_ID);
        final SessionDisconnectEvent event = new SessionDisconnectEvent(this, message(StompCommand.DISCONNECT, null), SESSION_ID, CloseStatus.NORMAL, PLAYER);

        assertThatCode(() -> handler.handleDisconnect(event)).doesNotThrowAnyException();
    }

    private Message<byte[]> message(final StompCommand command, final String destination) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setSessionId(SESSION_ID);
        accessor.setDestination(destination);

        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
