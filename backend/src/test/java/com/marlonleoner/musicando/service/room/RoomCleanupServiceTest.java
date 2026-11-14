package com.marlonleoner.musicando.service.room;

import com.marlonleoner.musicando.config.properties.CleanupProperties;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.service.game.GameEngine;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomCleanupServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-14T12:00:00Z");

    @Mock
    private RoomService roomService;

    @Mock
    private GameEngine gameEngine;

    @Mock
    private RoomStateNotifier notifier;

    private RoomCleanupService service;

    @BeforeEach
    void setUp() {
        service = new RoomCleanupService(roomService, gameEngine, notifier, new CleanupProperties(Duration.ofMinutes(30), Duration.ofHours(24)),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void shouldCloseAndNotifyAbandonedRoomsWhenTheTvIsGoneForTooLong() throws BaseException {
        final Room room = Fixture.make(Room.class);
        when(roomService.findAbandoned(Date.from(NOW.minus(Duration.ofMinutes(30))))).thenReturn(List.of(room));

        assertThat(service.closeAbandonedRooms()).isEqualTo(1);

        verify(gameEngine).stop(room.getCode());
        verify(roomService).close(room);
        verify(notifier).broadcast(room.getCode(), RoomStateReason.ROOM_CLOSED, null);
    }

    @Test
    void shouldSkipRoomWhenItCannotBeClosed() throws BaseException {
        final Room failing = Fixture.make(Room.class);
        final Room other = Fixture.make(Room.class);
        when(roomService.findAbandoned(any(Date.class))).thenReturn(List.of(failing, other));
        doThrow(new DataAccessResourceFailureException("Redis is down")).when(gameEngine).stop(failing.getCode());
        doThrow(new ObjectNotFoundException(Room.class)).when(notifier).broadcast(other.getCode(), RoomStateReason.ROOM_CLOSED, null);

        assertThat(service.closeAbandonedRooms()).isZero();
        verify(roomService, never()).close(failing);
    }

    @Test
    void shouldDeleteRoomsClosedBeforeRetention() {
        final Room room = Fixture.make(Room.class);
        when(roomService.findClosedBefore(Date.from(NOW.minus(Duration.ofHours(24))))).thenReturn(List.of(room));

        assertThat(service.deleteClosedRooms()).isEqualTo(1);

        verify(roomService).delete(room);
    }

    @Test
    void shouldSkipRoomWhenItCannotBeDeleted() {
        final Room room = Fixture.make(Room.class);
        when(roomService.findClosedBefore(any(Date.class))).thenReturn(List.of(room));
        doThrow(new DataAccessResourceFailureException("database is down")).when(roomService).delete(room);

        assertThat(service.deleteClosedRooms()).isZero();
    }

    @Test
    void shouldCloseAbandonedAndDeleteOldRoomsWhenCleaningUp() throws BaseException {
        final Room abandoned = Fixture.make(Room.class);
        final Room closed = Fixture.make(Room.class);
        when(roomService.findAbandoned(any(Date.class))).thenReturn(List.of(abandoned));
        when(roomService.findClosedBefore(any(Date.class))).thenReturn(List.of(closed));

        service.cleanUp();

        verify(roomService).close(abandoned);
        verify(roomService).delete(closed);
    }

    @Test
    void shouldDoNothingWhenThereIsNothingToClean() {
        when(roomService.findAbandoned(any(Date.class))).thenReturn(List.of());
        when(roomService.findClosedBefore(any(Date.class))).thenReturn(List.of());

        service.cleanUp();

        verify(roomService, never()).close(any());
        verify(roomService, never()).delete(any());
    }
}
