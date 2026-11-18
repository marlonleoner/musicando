package com.marlonleoner.musicando.service.game;

import com.marlonleoner.musicando.domain.Match;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.service.match.MatchService;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LobbyServiceTest {

    @Mock
    private RoomService roomService;

    @Mock
    private MatchService matchService;

    @Mock
    private PlayerService playerService;

    @Mock
    private GameEngine gameEngine;

    @Mock
    private LobbyMusicService lobbyMusicService;

    @InjectMocks
    private LobbyService service;

    @Test
    void shouldDiscardGameAndReopenLobbyWithNewMatchWhenGoingBackToLobby() {
        final Room room = Fixture.make(Room.class);
        final Match previous = room.getCurrentMatch();
        final Match next = Fixture.make(Match.class);
        next.setPlaylistId("123");
        when(matchService.createNext(room, previous)).thenReturn(next);

        service.backToLobby(room);

        final InOrder order = inOrder(gameEngine, matchService, roomService, playerService, lobbyMusicService);
        order.verify(gameEngine).stop(room.getCode());
        order.verify(matchService).finish(previous);
        order.verify(roomService).attachMatch(room, next);
        order.verify(playerService).resetResults(room.getPlayers());
        order.verify(roomService).reopen(room);
        order.verify(lobbyMusicService).refresh(room.getCode(), "123");
    }

    @Test
    void shouldNotRefreshLobbyMusicWhenNewMatchHasNoPlaylist() {
        final Room room = Fixture.make(Room.class);
        final Match next = Fixture.make(Match.class);
        next.setPlaylistId(null);
        when(matchService.createNext(room, room.getCurrentMatch())).thenReturn(next);

        service.backToLobby(room);

        verify(roomService).reopen(room);
        verifyNoInteractions(lobbyMusicService);
    }
}
