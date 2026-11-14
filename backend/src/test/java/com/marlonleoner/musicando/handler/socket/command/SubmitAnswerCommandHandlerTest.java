package com.marlonleoner.musicando.handler.socket.command;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.ErrorCode;
import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.enums.RoomStatus;
import com.marlonleoner.musicando.domain.exception.AnswerNotAcceptedException;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.SocketCommandException;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.service.game.GameEngine;
import com.marlonleoner.musicando.service.room.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitAnswerCommandHandlerTest {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private static final SocketPrincipal PRINCIPAL = new SocketPrincipal(RoleEnum.PLAYER, "player-1", "ABC123");

    @Mock
    private RoomService roomService;

    @Mock
    private GameEngine gameEngine;

    @InjectMocks
    private SubmitAnswerCommandHandler handler;

    private Room room(final RoomStatus status) throws BaseException {
        final Room room = Fixture.make(Room.class);
        room.setStatus(status);
        when(roomService.getOrException("ABC123")).thenReturn(room);

        return room;
    }

    @Test
    void shouldSubmitAnswerToEngineWhenGameIsRunning() throws BaseException {
        room(RoomStatus.IN_PROGRESS);

        handler.handle(PRINCIPAL, JSON_MAPPER.readTree("{\"round\":3,\"option\":2}"));

        verify(gameEngine).submitAnswer("ABC123", "player-1", 3, 2);
    }

    @ParameterizedTest
    @EnumSource(value = RoomStatus.class, names = "IN_PROGRESS", mode = EnumSource.Mode.EXCLUDE)
    void shouldThrowAnswerNotAcceptedExceptionWhenGameIsNotRunning(final RoomStatus status) throws BaseException {
        room(status);
        final JsonNode payload = JSON_MAPPER.readTree("{\"round\":1,\"option\":0}");

        assertThatThrownBy(() -> handler.handle(PRINCIPAL, payload)).isInstanceOf(AnswerNotAcceptedException.class);
        verifyNoInteractions(gameEngine);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"round\":1}", "{\"option\":1}", "{\"round\":\"1\",\"option\":0}", "{\"round\":1,\"option\":1.5}",
        "{\"round\":1,\"option\":null}", "{\"round\":99999999999,\"option\":0}"})
    void shouldThrowInvalidCommandWhenPayloadIsInvalid(final String json) {
        final JsonNode payload = JSON_MAPPER.readTree(json);

        assertInvalidCommand(payload);
    }

    @ParameterizedTest
    @NullSource
    void shouldThrowInvalidCommandWhenPayloadIsMissing(final JsonNode payload) {
        assertInvalidCommand(payload);
    }

    private void assertInvalidCommand(final JsonNode payload) {
        assertThatThrownBy(() -> handler.handle(PRINCIPAL, payload))
                .isInstanceOf(SocketCommandException.class)
                .extracting(ex -> ((SocketCommandException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_COMMAND);
        verifyNoInteractions(roomService, gameEngine);
    }
}
