package com.marlonleoner.musicando.controller;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.event.BroadcastEnum;
import com.marlonleoner.musicando.dto.response.RoomStateBroadcast;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.mapper.RoomMapper;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.SocketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocketControllerTest {

    private static final String CODE = "ABC123";

    @Mock
    private SocketService service;

    @Mock
    private RoomService roomService;

    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private SocketController controller;

    @Test
    void shouldBroadcastRoomStateWhenRoomExists() {
        final Room room = Fixture.make(Room.class);
        final RoomStateBroadcast state = Fixture.make(RoomStateBroadcast.class);
        when(roomService.findByCode(CODE)).thenReturn(Optional.of(room));
        when(roomMapper.toStateBroadcast(room)).thenReturn(state);

        controller.handleJoin(CODE);

        verify(service).broadcastAll(BroadcastEnum.UPDATE_ROOM, CODE, state);
    }

    @Test
    void shouldNotBroadcastWhenRoomDoesNotExist() {
        when(roomService.findByCode(CODE)).thenReturn(Optional.empty());

        controller.handleJoin(CODE);

        verifyNoInteractions(service, roomMapper);
        verify(roomService).findByCode(any());
    }
}
