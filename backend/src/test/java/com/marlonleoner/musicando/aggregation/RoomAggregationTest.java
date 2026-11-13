package com.marlonleoner.musicando.aggregation;

import com.marlonleoner.musicando.config.properties.RoomProperties;
import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.RoomStatus;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.InvalidNicknameException;
import com.marlonleoner.musicando.domain.exception.NicknameTakenException;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.domain.exception.RoomFullException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
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

    private static final int MAX_PLAYERS = 3;

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

    private RoomAggregation aggregation;

    @BeforeEach
    void setUp() {
        aggregation = new RoomAggregation(roomService, playerService, roomMapper, playerMapper, notifier, new RoomProperties(MAX_PLAYERS));
    }

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
    void shouldCreatePlayerWithNormalizedNicknameWhenRoomIsWaitingForPlayers() throws BaseException {
        final Room room = waitingRoom(0);
        final Player player = Fixture.make(Player.class);
        final JoinRoomResponse response = Fixture.make(JoinRoomResponse.class);
        when(roomService.getOrException(room.getCode())).thenReturn(room);
        when(playerService.createAndSave("ANA MARIA", room)).thenReturn(player);
        when(playerMapper.toJoinResponse(player)).thenReturn(response);

        assertThat(aggregation.joinRoom(new JoinRoomRequest(room.getCode(), "  ana   maria "))).isEqualTo(response);
        verify(playerService).ensureOwner(room.getCode());
        verify(notifier).broadcast(room.getCode(), RoomStateReason.PLAYER_JOINED, player.getId());
    }

    @ParameterizedTest
    @EnumSource(value = RoomStatus.class, names = "WAITING", mode = EnumSource.Mode.EXCLUDE)
    void shouldThrowRoomNotJoinableExceptionWhenRoomIsNotWaitingForPlayers(final RoomStatus status) throws BaseException {
        final Room room = waitingRoom(0);
        room.setStatus(status);
        when(roomService.getOrException(room.getCode())).thenReturn(room);

        assertJoinRejected(room, "ANA", RoomNotJoinableException.class);
    }

    @Test
    void shouldThrowRoomFullExceptionWhenRoomReachedMaximumOfPlayers() throws BaseException {
        final Room room = waitingRoom(MAX_PLAYERS);
        when(roomService.getOrException(room.getCode())).thenReturn(room);

        assertJoinRejected(room, "ANA", RoomFullException.class);
    }

    @Test
    void shouldThrowNicknameTakenExceptionWhenNicknameIsAlreadyInUseIgnoringCase() throws BaseException {
        final Room room = waitingRoom(1);
        room.getPlayers().getFirst().setNickname("ANA");
        when(roomService.getOrException(room.getCode())).thenReturn(room);

        assertJoinRejected(room, "ana", NicknameTakenException.class);
    }

    @Test
    void shouldThrowInvalidNicknameExceptionBeforeLoadingRoomWhenNicknameIsInvalid() {
        assertThatThrownBy(() -> aggregation.joinRoom(new JoinRoomRequest("ABC123", "a"))).isInstanceOf(InvalidNicknameException.class);
        verifyNoInteractions(roomService, playerService, notifier);
    }

    @Test
    void shouldPropagateObjectNotFoundExceptionWhenRoomDoesNotExist() throws BaseException {
        when(roomService.getOrException("ABC123")).thenThrow(new ObjectNotFoundException(Room.class));

        assertThatThrownBy(() -> aggregation.joinRoom(new JoinRoomRequest("ABC123", "ANA"))).isInstanceOf(ObjectNotFoundException.class);
        verifyNoInteractions(playerService, notifier);
    }

    private Room waitingRoom(final int players) {
        final Room room = Fixture.make(Room.class);
        room.setStatus(RoomStatus.WAITING);
        room.setPlayers(new ArrayList<>(Fixture.makeList(Player.class, players)));

        return room;
    }

    private void assertJoinRejected(final Room room, final String nickname, final Class<? extends Throwable> expected) {
        assertThatThrownBy(() -> aggregation.joinRoom(new JoinRoomRequest(room.getCode(), nickname))).isInstanceOf(expected);
        verifyNoInteractions(playerService, notifier);
    }
}
