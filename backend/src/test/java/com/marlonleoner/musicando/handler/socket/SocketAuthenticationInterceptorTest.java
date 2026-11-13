package com.marlonleoner.musicando.handler.socket;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.exception.SocketAuthenticationException;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.service.socket.SocketAuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocketAuthenticationInterceptorTest {

    private static final SocketPrincipal HOST = new SocketPrincipal(RoleEnum.HOST, "room-id", "ABC123");

    @Mock
    private SocketAuthenticationService authenticationService;

    @Mock
    private MessageChannel channel;

    @InjectMocks
    private SocketAuthenticationInterceptor interceptor;

    @Test
    void shouldSetPrincipalWhenConnectCredentialsAreValid() {
        when(authenticationService.authenticate("host", "ABC123", "room-id", "room-secret")).thenReturn(HOST);
        final StompHeaderAccessor accessor = accessor(StompCommand.CONNECT, null);
        accessor.addNativeHeader("role", "host");
        accessor.addNativeHeader("code", "ABC123");
        accessor.addNativeHeader("id", "room-id");
        accessor.addNativeHeader("secret", "room-secret");
        final Message<byte[]> message = message(accessor);

        interceptor.preSend(message, channel);

        assertThat(MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class).getUser()).isEqualTo(HOST);
    }

    @Test
    void shouldPropagateErrorWhenConnectCredentialsAreInvalid() {
        when(authenticationService.authenticate(null, null, null, null))
                .thenThrow(new SocketAuthenticationException(ErrorCode.INVALID_CREDENTIALS));
        final Message<byte[]> message = message(accessor(StompCommand.CONNECT, null));

        assertThatThrownBy(() -> interceptor.preSend(message, channel)).isInstanceOf(SocketAuthenticationException.class)
                .hasMessage("INVALID_CREDENTIALS");
    }

    @Test
    void shouldAuthorizeSubscriptionWhenPrincipalIsPresent() {
        final StompHeaderAccessor accessor = accessor(StompCommand.SUBSCRIBE, HOST);
        accessor.setDestination("/topic/room/ABC123");

        interceptor.preSend(message(accessor), channel);

        verify(authenticationService).authorizeSubscription(HOST, "/topic/room/ABC123");
    }

    @Test
    void shouldPropagateErrorWhenSubscriptionIsNotAllowed() {
        doThrow(new SocketAuthenticationException(ErrorCode.FORBIDDEN))
                .when(authenticationService).authorizeSubscription(HOST, "/topic/room/OTHER1");
        final StompHeaderAccessor accessor = accessor(StompCommand.SUBSCRIBE, HOST);
        accessor.setDestination("/topic/room/OTHER1");
        final Message<byte[]> message = message(accessor);

        assertThatThrownBy(() -> interceptor.preSend(message, channel)).hasMessage("FORBIDDEN");
    }

    @Test
    void shouldAuthorizeSendWhenPrincipalIsPresent() {
        final StompHeaderAccessor accessor = accessor(StompCommand.SEND, HOST);
        accessor.setDestination("/musicando/command");

        interceptor.preSend(message(accessor), channel);

        verify(authenticationService).authorizeSend("/musicando/command");
    }

    @Test
    void shouldRejectFrameWhenPrincipalIsMissing() {
        final StompHeaderAccessor accessor = accessor(StompCommand.SEND, null);
        accessor.setDestination("/musicando/command");
        final Message<byte[]> message = message(accessor);

        assertThatThrownBy(() -> interceptor.preSend(message, channel)).hasMessage("FORBIDDEN");
        verifyNoInteractions(authenticationService);
    }

    @Test
    void shouldIgnoreOtherFramesWhenCommandDoesNotRequireValidation() {
        final Message<byte[]> message = message(accessor(StompCommand.DISCONNECT, null));

        assertThat(interceptor.preSend(message, channel)).isSameAs(message);
        verifyNoInteractions(authenticationService);
    }

    @Test
    void shouldIgnoreMessagesWithoutStompHeaders() {
        final Message<String> message = MessageBuilder.withPayload("heartbeat").build();

        assertThat(interceptor.preSend(message, channel)).isSameAs(message);
        verifyNoInteractions(authenticationService);
    }

    private StompHeaderAccessor accessor(final StompCommand command, final SocketPrincipal user) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setSessionId("session-1");
        accessor.setUser(user);
        accessor.setLeaveMutable(true);

        return accessor;
    }

    private Message<byte[]> message(final StompHeaderAccessor accessor) {
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
