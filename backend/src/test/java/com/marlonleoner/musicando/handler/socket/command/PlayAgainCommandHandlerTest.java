package com.marlonleoner.musicando.handler.socket.command;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.ErrorCode;
import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.enums.RoomStatus;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.NotRoomOwnerException;
import com.marlonleoner.musicando.domain.exception.SocketCommandException;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.service.game.LobbyService;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayAgainCommandHandlerTest {

    private static final SocketPrincipal PRINCIPAL = new SocketPrincipal(RoleEnum.PLAYER, "player-1", "ABC123");

    @Mock
    private PlayerService playerService;

    @Mock
    private RoomService roomService;

    @Mock
    private LobbyService lobbyService;

    @Mock
    private RoomStateNotifier notifier;

    @InjectMocks
    private PlayAgainCommandHandler handler;

    private Room room(final RoomStatus status) throws BaseException {
        final Room room = Fixture.make(Room.class);
        room.setStatus(status);
        when(roomService.getOrException("ABC123")).thenReturn(room);

        return room;
    }

    @Test
    void shouldSendEveryoneBackToLobbyWhenGameIsFinished() throws BaseException {
        final Room room = room(RoomStatus.FINISHED);

        handler.handle(PRINCIPAL, null);

        final InOrder order = inOrder(lobbyService, notifier);
        order.verify(lobbyService).backToLobby(room);
        order.verify(notifier).broadcast("ABC123", RoomStateReason.PLAY_AGAIN, "player-1");
    }

    @ParameterizedTest
    @EnumSource(value = RoomStatus.class, names = "FINISHED", mode = EnumSource.Mode.EXCLUDE)
    void shouldThrowInvalidCommandWhenGameIsNotFinished(final RoomStatus status) throws BaseException {
        room(status);

        assertThatThrownBy(() -> handler.handle(PRINCIPAL, null))
                .isInstanceOf(SocketCommandException.class)
                .extracting(ex -> ((SocketCommandException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_COMMAND);
        verifyNoInteractions(lobbyService, notifier);
    }

    @Test
    void shouldThrowNotRoomOwnerExceptionWhenPlayerIsNotOwner() throws BaseException {
        when(playerService.getOwnerOrException("player-1")).thenThrow(new NotRoomOwnerException());

        assertThatThrownBy(() -> handler.handle(PRINCIPAL, null)).isInstanceOf(NotRoomOwnerException.class);
        verifyNoInteractions(roomService, lobbyService, notifier);
    }
}
