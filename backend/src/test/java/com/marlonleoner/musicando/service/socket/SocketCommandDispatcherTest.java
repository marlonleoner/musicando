package com.marlonleoner.musicando.service.socket;

import com.marlonleoner.musicando.config.properties.RateLimitProperties;
import com.marlonleoner.musicando.domain.enums.ErrorCode;
import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.SocketCommandException;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.dto.socket.SocketCommand;
import com.marlonleoner.musicando.handler.socket.command.GetRoomStateCommandHandler;
import com.marlonleoner.musicando.service.ratelimit.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocketCommandDispatcherTest {

    private static final int COMMANDS_PER_SECOND = 3;

    @Mock
    private ApplicationContext context;

    @Mock
    private GetRoomStateCommandHandler getRoomStateHandler;

    private SocketCommandDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new SocketCommandDispatcher(context, new RateLimiter(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)),
                new RateLimitProperties(10, 20, COMMANDS_PER_SECOND, 60));
    }

    @ParameterizedTest
    @EnumSource(RoleEnum.class)
    void shouldDelegateToHandlerWhenRoleIsAllowed(final RoleEnum role) throws BaseException {
        final SocketPrincipal principal = new SocketPrincipal(role, "participant-1", "ABC123");
        when(context.getBean(GetRoomStateCommandHandler.class)).thenReturn(getRoomStateHandler);

        dispatcher.dispatch(principal, new SocketCommand("GET_ROOM_STATE", null));

        verify(getRoomStateHandler).handle(principal, null);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"UNKNOWN", "get_room_state", ""})
    void shouldThrowInvalidCommandWhenTypeIsUnknown(final String type) {
        final SocketPrincipal principal = new SocketPrincipal(RoleEnum.HOST, "room-id", "ABC123");

        assertThatThrownBy(() -> dispatcher.dispatch(principal, new SocketCommand(type, null)))
                .isInstanceOf(SocketCommandException.class)
                .extracting(ex -> ((SocketCommandException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_COMMAND);
        verifyNoInteractions(context);
    }

    @ParameterizedTest
    @CsvSource({"LEAVE_ROOM, HOST", "KICK_PLAYER, PLAYER", "CLOSE_ROOM, PLAYER", "UPDATE_SETTINGS, HOST", "START_GAME, HOST", "SUBMIT_ANSWER, HOST", "PLAY_AGAIN, HOST"})
    void shouldThrowForbiddenWhenRoleIsNotAllowed(final String type, final RoleEnum role) {
        final SocketPrincipal principal = new SocketPrincipal(role, "participant-1", "ABC123");

        assertThatThrownBy(() -> dispatcher.dispatch(principal, new SocketCommand(type, null)))
                .isInstanceOf(SocketCommandException.class)
                .extracting(ex -> ((SocketCommandException) ex).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
        verifyNoInteractions(context);
    }

    @Test
    void shouldRejectCommandsWhenParticipantExceedsLimitPerSecond() throws BaseException {
        final SocketPrincipal principal = new SocketPrincipal(RoleEnum.PLAYER, "participant-1", "ABC123");
        when(context.getBean(GetRoomStateCommandHandler.class)).thenReturn(getRoomStateHandler);

        for (int i = 0; i < COMMANDS_PER_SECOND; i++) {
            dispatcher.dispatch(principal, new SocketCommand("GET_ROOM_STATE", null));
        }

        assertThatThrownBy(() -> dispatcher.dispatch(principal, new SocketCommand("GET_ROOM_STATE", null)))
                .isInstanceOf(SocketCommandException.class)
                .extracting(ex -> ((SocketCommandException) ex).getErrorCode())
                .isEqualTo(ErrorCode.RATE_LIMITED);
        dispatcher.dispatch(new SocketPrincipal(RoleEnum.PLAYER, "participant-2", "ABC123"), new SocketCommand("GET_ROOM_STATE", null));
    }
}
