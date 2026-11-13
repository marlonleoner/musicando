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
import com.marlonleoner.musicando.service.match.MatchService;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.SocketService;
import com.marlonleoner.musicando.util.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HostConnectHandlerTest {

    @Mock
    private RedisService redisService;

    @Mock
    private RoomService roomService;

    @Mock
    private MatchService matchService;

    @Mock
    private SocketService socketService;

    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private HostConnectHandler handler;

    @Test
    void shouldConnectHostAndBroadcastRoomStateWhenSecretsAreValid() throws BaseException {
        final Room room = Fixture.make(Room.class);
        final RoomStateBroadcast state = Fixture.make(RoomStateBroadcast.class);
        final SocketAttributes attributes = attributesFor(room.getId(), room.getSecret(), room.getCode());
        when(roomService.getOrException(room.getCode())).thenReturn(room);
        when(roomMapper.toStateBroadcast(room)).thenReturn(state);

        handler.handle(attributes);

        verify(redisService).set(String.format(Constants.REDIS_HOST_CONNECTED, room.getCode()), true, Constants.SESSION_TTL_SECONDS);
        verify(roomService).onConnect(room);
        verify(matchService).onConnect(room.getCurrentMatch());
        verify(socketService).broadcastAll(BroadcastEnum.UPDATE_ROOM, room.getCode(), state);
    }

    @Test
    void shouldThrowBaseExceptionWhenSecretsAreInvalid() throws BaseException {
        final Room room = Fixture.make(Room.class);
        final SocketAttributes attributes = attributesFor(room.getId(), "invalid-secret", room.getCode());
        when(roomService.getOrException(room.getCode())).thenReturn(room);

        assertThatThrownBy(() -> handler.handle(attributes)).isInstanceOf(BaseException.class);
        verify(roomService).getOrException(room.getCode());
        verifyNoInteractions(redisService, matchService, socketService, roomMapper);
    }

    @Test
    void shouldThrowObjectNotFoundExceptionWhenRoomDoesNotExist() throws BaseException {
        final SocketAttributes attributes = attributesFor("room-id", "room-secret", "ABC123");
        when(roomService.getOrException(any())).thenThrow(new ObjectNotFoundException(Room.class));

        assertThatThrownBy(() -> handler.handle(attributes)).isInstanceOf(ObjectNotFoundException.class);
        verifyNoInteractions(redisService, matchService, socketService, roomMapper);
    }

    private SocketAttributes attributesFor(final String id, final String secret, final String code) {
        return new SocketAttributes("session-1", id, code, secret, RoleEnum.HOST);
    }
}
