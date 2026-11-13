package com.marlonleoner.musicando.service.socket;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.domain.socket.SocketEventType;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.dto.response.RoomStateBroadcast;
import com.marlonleoner.musicando.dto.socket.SocketEvent;
import com.marlonleoner.musicando.dto.socket.SocketEventReason;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.mapper.RoomMapper;
import com.marlonleoner.musicando.service.room.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomStateNotifierTest {

    private static final String CODE = "ABC123";

    @Mock
    private RoomService roomService;

    @Mock
    private RoomMapper roomMapper;

    @Mock
    private SocketService socketService;

    @InjectMocks
    private RoomStateNotifier notifier;

    @Test
    void shouldBroadcastRoomStateWithReasonWhenBroadcasting() throws ObjectNotFoundException {
        final RoomStateBroadcast state = mockState();

        notifier.broadcast(CODE, RoomStateReason.PLAYER_JOINED, "player-1");

        final ArgumentCaptor<SocketEvent> captor = ArgumentCaptor.forClass(SocketEvent.class);
        verify(socketService).broadcastToRoom(eq(CODE), captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(SocketEventType.ROOM_STATE);
        assertThat(captor.getValue().payload()).isEqualTo(state);
        assertThat(captor.getValue().reason()).isEqualTo(new SocketEventReason(RoomStateReason.PLAYER_JOINED, "player-1"));
        assertThat(captor.getValue().timestamp()).isNotNull();
    }

    @Test
    void shouldSendSnapshotOnlyToParticipantWhenSendingToPrincipal() throws ObjectNotFoundException {
        final RoomStateBroadcast state = mockState();

        notifier.sendTo(new SocketPrincipal(RoleEnum.PLAYER, "player-1", CODE));

        final ArgumentCaptor<SocketEvent> captor = ArgumentCaptor.forClass(SocketEvent.class);
        verify(socketService).sendToParticipant(eq("player-1"), captor.capture());
        assertThat(captor.getValue().payload()).isEqualTo(state);
        assertThat(captor.getValue().reason().type()).isEqualTo(RoomStateReason.SNAPSHOT);
    }

    @Test
    void shouldBuildSnapshotWithoutSendingWhenBuildingSnapshot() throws ObjectNotFoundException {
        final RoomStateBroadcast state = mockState();

        final SocketEvent snapshot = notifier.snapshot(new SocketPrincipal(RoleEnum.HOST, "room-id", CODE));

        assertThat(snapshot.payload()).isEqualTo(state);
        assertThat(snapshot.reason().type()).isEqualTo(RoomStateReason.SNAPSHOT);
        verifyNoInteractions(socketService);
    }

    @Test
    void shouldPropagateObjectNotFoundExceptionWhenRoomDoesNotExist() throws ObjectNotFoundException {
        when(roomService.getOrException(CODE)).thenThrow(new ObjectNotFoundException(Room.class));

        assertThatThrownBy(() -> notifier.broadcast(CODE, RoomStateReason.HOST_CONNECTED, "room-id"))
                .isInstanceOf(ObjectNotFoundException.class);
        verifyNoInteractions(socketService);
    }

    private RoomStateBroadcast mockState() throws ObjectNotFoundException {
        final Room room = Fixture.make(Room.class);
        final RoomStateBroadcast state = Fixture.make(RoomStateBroadcast.class);
        when(roomService.getOrException(CODE)).thenReturn(room);
        when(roomMapper.toStateBroadcast(room)).thenReturn(state);

        return state;
    }
}
