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
class HostDisconnectHandlerTest {

    @Mock
    private RedisService redisService;

    @Mock
    private SocketService socketService;

    @Mock
    private RoomService roomService;

    @Mock
    private MatchService matchService;

    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private HostDisconnectHandler handler;

    @Test
    void shouldDisconnectHostAndBroadcastRoomStateWhenRoomExists() throws BaseException {
        final Room room = Fixture.make(Room.class);
        final RoomStateBroadcast state = Fixture.make(RoomStateBroadcast.class);
        final SocketAttributes attributes = new SocketAttributes("session-1", room.getId(), room.getCode(), room.getSecret(), RoleEnum.HOST);
        when(roomService.getOrException(room.getCode())).thenReturn(room);
        when(roomMapper.toStateBroadcast(room)).thenReturn(state);

        handler.handle(attributes);

        verify(redisService).set(String.format(Constants.REDIS_HOST_CONNECTED, room.getCode()), false, Constants.SESSION_TTL_SECONDS);
        verify(roomService).onDisconnect(room);
        verify(matchService).onDisconnect(room.getCurrentMatch());
        verify(socketService).broadcastAll(BroadcastEnum.UPDATE_ROOM, room.getCode(), state);
    }

    @Test
    void shouldThrowObjectNotFoundExceptionWhenRoomDoesNotExist() throws BaseException {
        final SocketAttributes attributes = new SocketAttributes("session-1", "room-id", "ABC123", "room-secret", RoleEnum.HOST);
        when(roomService.getOrException(any())).thenThrow(new ObjectNotFoundException(Room.class));

        assertThatThrownBy(() -> handler.handle(attributes)).isInstanceOf(ObjectNotFoundException.class);
        verifyNoInteractions(redisService, matchService, socketService, roomMapper);
    }
}
