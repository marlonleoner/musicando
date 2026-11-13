package com.marlonleoner.musicando.handler.socket.command;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveRoomCommandHandlerTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private RoomStateNotifier notifier;

    @InjectMocks
    private LeaveRoomCommandHandler handler;

    @Test
    void shouldRemovePlayerAndNotifyRoomWhenPlayerLeaves() throws BaseException {
        final Player player = Fixture.make(Player.class);
        final SocketPrincipal principal = new SocketPrincipal(RoleEnum.PLAYER, player.getId(), "ABC123");
        when(playerService.getOrException(player.getId())).thenReturn(player);

        handler.handle(principal, null);

        final InOrder order = inOrder(playerService, notifier);
        order.verify(playerService).delete(player);
        order.verify(playerService).ensureOwner("ABC123");
        order.verify(notifier).broadcast("ABC123", RoomStateReason.PLAYER_LEFT, player.getId());
    }

    @Test
    void shouldPropagateObjectNotFoundExceptionWhenPlayerAlreadyLeft() throws BaseException {
        final SocketPrincipal principal = new SocketPrincipal(RoleEnum.PLAYER, "player-1", "ABC123");
        when(playerService.getOrException("player-1")).thenThrow(new ObjectNotFoundException(Player.class));

        assertThatThrownBy(() -> handler.handle(principal, null)).isInstanceOf(ObjectNotFoundException.class);
        verifyNoInteractions(notifier);
    }
}
