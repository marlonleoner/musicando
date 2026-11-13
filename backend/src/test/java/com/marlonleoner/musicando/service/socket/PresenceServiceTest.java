package com.marlonleoner.musicando.service.socket;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.handler.socket.SocketSessionRegistry;
import com.marlonleoner.musicando.service.match.MatchService;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresenceServiceTest {

    private static final String SESSION_ID = "session-new";

    @Mock
    private RoomService roomService;

    @Mock
    private PlayerService playerService;

    @Mock
    private MatchService matchService;

    @Mock
    private RoomStateNotifier notifier;

    @Mock
    private SocketSessionRegistry sessionRegistry;

    @InjectMocks
    private PresenceService service;

    @Test
    void shouldConnectHostAndNotifyRoomWhenHostConnects() throws ObjectNotFoundException {
        final Room room = hostRoom(null);

        service.onConnected(hostOf(room), SESSION_ID);

        verify(roomService).onConnect(room, SESSION_ID);
        verify(matchService).onConnect(room.getCurrentMatch());
        verify(notifier).broadcast(room.getCode(), RoomStateReason.HOST_CONNECTED, room.getId());
        verifyNoInteractions(sessionRegistry);
    }

    @Test
    void shouldClosePreviousHostSessionWhenHostConnectsAgain() throws ObjectNotFoundException {
        final Room room = hostRoom("session-old");

        service.onConnected(hostOf(room), SESSION_ID);

        verify(sessionRegistry).closeReplaced("session-old");
        verify(roomService).onConnect(room, SESSION_ID);
    }

    @Test
    void shouldDisconnectHostAndNotifyRoomWhenCurrentHostSessionCloses() throws ObjectNotFoundException {
        final Room room = hostRoom(SESSION_ID);

        service.onDisconnected(hostOf(room), SESSION_ID);

        verify(roomService).onDisconnect(room);
        verify(matchService).onDisconnect(room.getCurrentMatch());
        verify(notifier).broadcast(room.getCode(), RoomStateReason.HOST_DISCONNECTED, room.getId());
    }

    @Test
    void shouldIgnoreDisconnectionWhenHostSessionWasReplaced() throws ObjectNotFoundException {
        final Room room = hostRoom(SESSION_ID);

        service.onDisconnected(hostOf(room), "session-old");

        verify(roomService, never()).onDisconnect(any());
        verifyNoInteractions(matchService, notifier);
    }

    @Test
    void shouldConnectPlayerAndNotifyRoomWhenPlayerConnects() throws ObjectNotFoundException {
        final Player player = player(null);

        service.onConnected(playerOf(player), SESSION_ID);

        verify(playerService).onConnect(player, SESSION_ID);
        verify(notifier).broadcast(player.getRoom().getCode(), RoomStateReason.PLAYER_CONNECTED, player.getId());
        verifyNoInteractions(sessionRegistry, matchService);
    }

    @Test
    void shouldClosePreviousPlayerSessionWhenPlayerConnectsAgain() throws ObjectNotFoundException {
        final Player player = player("session-old");

        service.onConnected(playerOf(player), SESSION_ID);

        verify(sessionRegistry).closeReplaced("session-old");
    }

    @Test
    void shouldNotCloseSessionWhenPreviousSessionIsTheSame() throws ObjectNotFoundException {
        final Player player = player(SESSION_ID);

        service.onConnected(playerOf(player), SESSION_ID);

        verifyNoInteractions(sessionRegistry);
    }

    @Test
    void shouldDisconnectPlayerAndNotifyRoomWhenCurrentPlayerSessionCloses() throws ObjectNotFoundException {
        final Player player = player(SESSION_ID);

        service.onDisconnected(playerOf(player), SESSION_ID);

        verify(playerService).onDisconnect(player);
        verify(notifier).broadcast(player.getRoom().getCode(), RoomStateReason.PLAYER_DISCONNECTED, player.getId());
    }

    @Test
    void shouldIgnoreDisconnectionWhenPlayerSessionWasReplaced() throws ObjectNotFoundException {
        final Player player = player(SESSION_ID);

        service.onDisconnected(playerOf(player), "session-old");

        verify(playerService, never()).onDisconnect(any());
        verifyNoInteractions(notifier);
    }

    private Room hostRoom(final String hostSessionId) throws ObjectNotFoundException {
        final Room room = Fixture.make(Room.class);
        room.setHostSessionId(hostSessionId);
        when(roomService.getOrException(room.getCode())).thenReturn(room);

        return room;
    }

    private Player player(final String sessionId) throws ObjectNotFoundException {
        final Player player = Fixture.make(Player.class);
        player.setSessionId(sessionId);
        when(playerService.getOrException(player.getId())).thenReturn(player);

        return player;
    }

    private SocketPrincipal hostOf(final Room room) {
        return new SocketPrincipal(RoleEnum.HOST, room.getId(), room.getCode());
    }

    private SocketPrincipal playerOf(final Player player) {
        return new SocketPrincipal(RoleEnum.PLAYER, player.getId(), player.getRoom().getCode());
    }
}
