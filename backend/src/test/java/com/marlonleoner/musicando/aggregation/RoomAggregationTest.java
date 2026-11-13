package com.marlonleoner.musicando.aggregation;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.RoomStatus;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.domain.exception.RoomNotJoinableException;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.dto.request.JoinRoomRequest;
import com.marlonleoner.musicando.dto.response.JoinRoomResponse;
import com.marlonleoner.musicando.dto.response.RoomCreatedResponse;
import com.marlonleoner.musicando.dto.response.RoomResponse;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.mapper.PlayerMapper;
import com.marlonleoner.musicando.mapper.RoomMapper;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomAggregationTest {

    @Mock
    private RoomService roomService;

    @Mock
    private PlayerService playerService;

    @Mock
    private RoomMapper roomMapper;

    @Mock
    private PlayerMapper playerMapper;

    @Mock
    private RoomStateNotifier notifier;

    @InjectMocks
    private RoomAggregation aggregation;

    @Test
    void shouldReturnMappedRoomsWhenGettingAllRooms() {
        final List<Room> rooms = Fixture.makeList(Room.class, 2);
        final List<RoomResponse> responses = Fixture.makeList(RoomResponse.class, 2);
        when(roomService.findAll()).thenReturn(rooms);
        when(roomMapper.toResponse(rooms)).thenReturn(responses);

        assertThat(aggregation.getAllRooms()).isEqualTo(responses);
    }

    @Test
    void shouldReturnCreatedRoomWhenCreatingRoom() {
        final Room room = Fixture.make(Room.class);
        final RoomCreatedResponse response = Fixture.make(RoomCreatedResponse.class);
        when(roomService.createAndSave()).thenReturn(room);
        when(roomMapper.toCreatedResponse(room)).thenReturn(response);

        assertThat(aggregation.createRoom()).isEqualTo(response);
    }

    @Test
    void shouldCreatePlayerWhenRoomIsWaitingForPlayers() throws BaseException {
        final JoinRoomRequest request = Fixture.make(JoinRoomRequest.class);
        final Room room = Fixture.make(Room.class);
        room.setStatus(RoomStatus.WAITING);
        final Player player = Fixture.make(Player.class);
        final JoinRoomResponse response = Fixture.make(JoinRoomResponse.class);
        when(roomService.getOrException(request.code())).thenReturn(room);
        when(playerService.createAndSave(request.nickname(), room)).thenReturn(player);
        when(playerMapper.toJoinResponse(player)).thenReturn(response);

        assertThat(aggregation.joinRoom(request)).isEqualTo(response);
        verify(notifier).broadcast(room.getCode(), RoomStateReason.PLAYER_JOINED, player.getId());
    }

    @ParameterizedTest
    @EnumSource(value = RoomStatus.class, names = "WAITING", mode = EnumSource.Mode.EXCLUDE)
    void shouldThrowRoomNotJoinableExceptionWhenRoomIsNotWaitingForPlayers(final RoomStatus status) throws BaseException {
        final JoinRoomRequest request = Fixture.make(JoinRoomRequest.class);
        final Room room = Fixture.make(Room.class);
        room.setStatus(status);
        when(roomService.getOrException(request.code())).thenReturn(room);

        assertThatThrownBy(() -> aggregation.joinRoom(request)).isInstanceOf(RoomNotJoinableException.class);
        verify(playerService, never()).createAndSave(any(), any());
        verifyNoInteractions(notifier);
    }

    @Test
    void shouldPropagateObjectNotFoundExceptionWhenRoomDoesNotExist() throws BaseException {
        final JoinRoomRequest request = Fixture.make(JoinRoomRequest.class);
        when(roomService.getOrException(request.code())).thenThrow(new ObjectNotFoundException(Room.class));

        assertThatThrownBy(() -> aggregation.joinRoom(request)).isInstanceOf(ObjectNotFoundException.class);
        verify(playerService, never()).createAndSave(any(), any());
        verifyNoInteractions(notifier);
    }
}
