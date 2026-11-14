package com.marlonleoner.musicando.service.game;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.service.match.MatchService;
import com.marlonleoner.musicando.service.room.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameRecoveryListenerTest {

    @Mock
    private RoomService roomService;

    @Mock
    private MatchService matchService;

    @Mock
    private GameEngine gameEngine;

    @Mock
    private LobbyService lobbyService;

    @InjectMocks
    private GameRecoveryListener listener;

    @Test
    void shouldPauseRoomAndMatchWhenGameStateCanBeRecovered() {
        final Room room = Fixture.make(Room.class);
        when(roomService.findInterrupted()).thenReturn(List.of(room));
        when(gameEngine.recover(room.getCode())).thenReturn(true);

        listener.recoverInterruptedGames();

        verify(roomService).pause(room);
        verify(matchService).onDisconnect(room.getCurrentMatch());
        verifyNoInteractions(lobbyService);
    }

    @Test
    void shouldSendRoomBackToLobbyWhenGameStateWasLost() {
        final Room room = Fixture.make(Room.class);
        when(roomService.findInterrupted()).thenReturn(List.of(room));
        when(gameEngine.recover(room.getCode())).thenReturn(false);

        listener.recoverInterruptedGames();

        verify(lobbyService).backToLobby(room);
        verify(roomService, never()).pause(room);
    }

    @Test
    void shouldContinueWithOtherRoomsWhenInfrastructureFails() {
        final Room failing = Fixture.make(Room.class);
        final Room other = Fixture.make(Room.class);
        when(roomService.findInterrupted()).thenReturn(List.of(failing, other));
        when(gameEngine.recover(failing.getCode())).thenThrow(new DataAccessResourceFailureException("Redis is down"));
        when(gameEngine.recover(other.getCode())).thenReturn(true);

        assertThatCode(listener::recoverInterruptedGames).doesNotThrowAnyException();
        verify(roomService).pause(other);
    }
}
