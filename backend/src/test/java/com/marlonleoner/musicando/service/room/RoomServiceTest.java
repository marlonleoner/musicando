package com.marlonleoner.musicando.service.room;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.RoomStatus;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    private static final String CODE = "ABC123";

    @Mock
    private RoomRepository repository;

    @InjectMocks
    private RoomService service;

    @Test
    void shouldReturnAllRoomsWhenFindingAll() {
        final List<Room> rooms = Fixture.makeList(Room.class, 2);
        when(repository.findAll()).thenReturn(rooms);

        assertThat(service.findAll()).isEqualTo(rooms);
    }

    @Test
    void shouldReturnRoomWhenFindingById() {
        final Room room = Fixture.make(Room.class);
        when(repository.findById(room.getId())).thenReturn(Optional.of(room));

        assertThat(service.findById(room.getId())).contains(room);
    }

    @Test
    void shouldReturnRoomWhenCodeExists() throws ObjectNotFoundException {
        final Room room = Fixture.make(Room.class);
        when(repository.findByCode(CODE)).thenReturn(Optional.of(room));

        assertThat(service.findByCode(CODE)).contains(room);
        assertThat(service.getOrException(CODE)).isEqualTo(room);
    }

    @Test
    void shouldThrowObjectNotFoundExceptionWhenCodeDoesNotExist() {
        when(repository.findByCode(CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOrException(CODE)).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    void shouldCreateWaitingRoomWithRandomCodeWhenCreatingRoom() {
        when(repository.existsByCode(anyString())).thenReturn(false);
        when(repository.saveAndFlush(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final Room room = service.createAndSave();

        assertThat(room.getCode()).matches("[A-Z0-9]{6}");
        assertThat(room.getStatus()).isEqualTo(RoomStatus.WAITING);
        assertThat(room.getId()).isNotBlank();
        assertThat(room.getSecret()).isNotBlank().isNotEqualTo(room.getId());
        assertThat(room.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldGenerateAnotherCodeWhenCodeAlreadyExists() {
        when(repository.existsByCode(anyString())).thenReturn(true, false);
        when(repository.saveAndFlush(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createAndSave();

        verify(repository, times(2)).existsByCode(anyString());
    }

    @Test
    void shouldRetryWhenCodeCollidesOnSave() {
        when(repository.existsByCode(anyString())).thenReturn(false);
        when(repository.saveAndFlush(any(Room.class)))
                .thenThrow(new DataIntegrityViolationException("duplicated code"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.createAndSave()).isNotNull();
        verify(repository, times(2)).saveAndFlush(any(Room.class));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenCodeCollidesOnEveryAttempt() {
        when(repository.existsByCode(anyString())).thenReturn(false);
        when(repository.saveAndFlush(any(Room.class))).thenThrow(new DataIntegrityViolationException("duplicated code"));

        assertThatThrownBy(() -> service.createAndSave()).isInstanceOf(IllegalStateException.class);
        verify(repository, times(5)).saveAndFlush(any(Room.class));
    }

    @ParameterizedTest
    @CsvSource({
        "WAITING, WAITING",
        "IN_PROGRESS, IN_PROGRESS",
        "PAUSED, IN_PROGRESS",
        "FINISHED, FINISHED",
        "CLOSED, CLOSED",
    })
    void shouldMarkRoomAsConnectedAndResumePausedMatchWhenHostConnects(final RoomStatus current, final RoomStatus expected) {
        final Room room = Fixture.make(Room.class);
        room.setStatus(current);
        room.setConnected(false);

        service.onConnect(room, "session-1");

        final Room saved = captureSavedRoom();
        assertThat(saved.isConnected()).isTrue();
        assertThat(saved.getHostSessionId()).isEqualTo("session-1");
        assertThat(saved.getStatus()).isEqualTo(expected);
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @ParameterizedTest
    @CsvSource({
        "WAITING, WAITING",
        "IN_PROGRESS, PAUSED",
        "PAUSED, PAUSED",
        "FINISHED, FINISHED",
        "CLOSED, CLOSED",
    })
    void shouldMarkRoomAsDisconnectedAndPauseMatchInProgressWhenHostDisconnects(final RoomStatus current, final RoomStatus expected) {
        final Room room = Fixture.make(Room.class);
        room.setStatus(current);
        room.setConnected(true);

        service.onDisconnect(room);

        final Room saved = captureSavedRoom();
        assertThat(saved.isConnected()).isFalse();
        assertThat(saved.getHostSessionId()).isNull();
        assertThat(saved.getStatus()).isEqualTo(expected);
    }

    @Test
    void shouldReturnUpdatedRoomsWhenResettingConnections() {
        when(repository.resetConnections()).thenReturn(3);

        assertThat(service.resetConnections()).isEqualTo(3);
    }

    private Room captureSavedRoom() {
        final ArgumentCaptor<Room> captor = ArgumentCaptor.forClass(Room.class);
        verify(repository).save(captor.capture());

        return captor.getValue();
    }
}
