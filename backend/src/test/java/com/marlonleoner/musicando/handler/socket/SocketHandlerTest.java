package com.marlonleoner.musicando.handler.socket;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.event.BroadcastEnum;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.domain.socket.SocketAttributes;
import com.marlonleoner.musicando.dto.response.RoomStateBroadcast;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.mapper.RoomMapper;
import com.marlonleoner.musicando.service.cache.RedisService;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.SocketService;
import com.marlonleoner.musicando.util.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocketHandlerTest {

    private static final String SESSION_ID = "session-1";

    private static final String SESSION_KEY = String.format(Constants.REDIS_SESSIONS, SESSION_ID);

    private static final String CODE = "ABC123";

    private static final SocketAttributes HOST = new SocketAttributes(SESSION_ID, "room-id", CODE, "room-secret", RoleEnum.HOST);

    @Mock
    private SocketService service;

    @Mock
    private RedisService redisService;

    @Mock
    private RoomService roomService;

    @Mock
    private RoomMapper roomMapper;

    @Mock
    private ApplicationContext context;

    @Mock
    private HostConnectHandler hostConnectHandler;

    @Mock
    private HostDisconnectHandler hostDisconnectHandler;

    @InjectMocks
    private SocketHandler handler;

    @Test
    void shouldStoreSessionAndDelegateToRoleHandlerWhenConnecting() throws BaseException {
        when(context.getBean(HostConnectHandler.class)).thenReturn(hostConnectHandler);

        handler.handleConnect(new SessionConnectEvent(this, connectMessage(Map.of("id", "room-id", "code", CODE, "secret", "room-secret", "role", "host"))));

        verify(redisService).set(SESSION_KEY, HOST, Constants.SESSION_TTL_SECONDS);
        verify(hostConnectHandler).handle(HOST);
    }

    @Test
    void shouldIgnoreConnectionWhenRoleIsInvalid() {
        handler.handleConnect(new SessionConnectEvent(this, connectMessage(Map.of("code", CODE, "role", "admin"))));

        verifyNoInteractions(redisService, context);
    }

    @Test
    void shouldIgnoreConnectionWhenRoleIsMissing() {
        handler.handleConnect(new SessionConnectEvent(this, connectMessage(Map.of("code", CODE))));

        verifyNoInteractions(redisService, context);
    }

    @Test
    void shouldNotPropagateErrorsWhenRoleHandlerFails() throws BaseException {
        when(context.getBean(HostConnectHandler.class)).thenReturn(hostConnectHandler);
        doThrow(new BaseException("Connection failed")).when(hostConnectHandler).handle(any());
        final SessionConnectEvent event = new SessionConnectEvent(this, connectMessage(Map.of("id", "room-id", "code", CODE, "secret", "room-secret", "role", "host")));

        assertThatCode(() -> handler.handleConnect(event)).doesNotThrowAnyException();
    }

    @Test
    void shouldInvalidateSessionAndDelegateToRoleHandlerWhenDisconnecting() throws BaseException {
        when(redisService.get(SESSION_KEY, SocketAttributes.class)).thenReturn(Optional.of(HOST));
        when(context.getBean(HostDisconnectHandler.class)).thenReturn(hostDisconnectHandler);

        handler.handleDisconnect(disconnectEvent());

        verify(redisService).invalidate(SESSION_KEY);
        verify(hostDisconnectHandler).handle(HOST);
    }

    @Test
    void shouldDoNothingWhenDisconnectingUnknownSession() {
        when(redisService.get(SESSION_KEY, SocketAttributes.class)).thenReturn(Optional.empty());

        handler.handleDisconnect(disconnectEvent());

        verify(redisService, never()).invalidate(anyString());
        verifyNoInteractions(context);
    }

    @Test
    void shouldBroadcastRoomStateWhenSubscribingToRoomTopic() throws ObjectNotFoundException {
        final Room room = Fixture.make(Room.class);
        final RoomStateBroadcast state = Fixture.make(RoomStateBroadcast.class);
        when(redisService.get(SESSION_KEY, SocketAttributes.class)).thenReturn(Optional.of(HOST));
        when(roomService.getOrException(CODE)).thenReturn(room);
        when(roomMapper.toStateBroadcast(room)).thenReturn(state);

        handler.handleSubscribe(new SessionSubscribeEvent(this, subscribeMessage(Constants.EVENT_UPDATE_ROOM + CODE)));

        verify(service).broadcastAll(BroadcastEnum.UPDATE_ROOM, room.getCode(), state);
    }

    @Test
    void shouldNotBroadcastWhenSubscribedRoomDoesNotExist() throws ObjectNotFoundException {
        when(redisService.get(SESSION_KEY, SocketAttributes.class)).thenReturn(Optional.of(HOST));
        when(roomService.getOrException(CODE)).thenThrow(new ObjectNotFoundException(Room.class));

        handler.handleSubscribe(new SessionSubscribeEvent(this, subscribeMessage(Constants.EVENT_UPDATE_ROOM + CODE)));

        verifyNoInteractions(service, roomMapper);
    }

    @Test
    void shouldIgnoreSubscriptionWhenDestinationIsNotRoomTopic() {
        handler.handleSubscribe(new SessionSubscribeEvent(this, subscribeMessage("/user/queue/room/host")));

        verifyNoInteractions(redisService, roomService, service);
    }

    @Test
    void shouldIgnoreSubscriptionWhenDestinationIsMissing() {
        handler.handleSubscribe(new SessionSubscribeEvent(this, subscribeMessage(null)));

        verifyNoInteractions(redisService, roomService, service);
        verify(roomService, never()).findByCode(eq(CODE));
    }

    private Message<byte[]> connectMessage(final Map<String, String> headers) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setSessionId(SESSION_ID);
        headers.forEach(accessor::addNativeHeader);

        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Message<byte[]> subscribeMessage(final String destination) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setSessionId(SESSION_ID);
        accessor.setDestination(destination);

        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private SessionDisconnectEvent disconnectEvent() {
        final StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accessor.setSessionId(SESSION_ID);

        return new SessionDisconnectEvent(this, MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders()), SESSION_ID, CloseStatus.NORMAL);
    }
}
