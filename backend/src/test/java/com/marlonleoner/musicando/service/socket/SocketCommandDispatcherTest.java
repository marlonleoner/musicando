package com.marlonleoner.musicando.service.socket;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.SocketCommandException;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.dto.socket.SocketCommand;
import com.marlonleoner.musicando.handler.socket.command.GetRoomStateCommandHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocketCommandDispatcherTest {

    @Mock
    private ApplicationContext context;

    @Mock
    private GetRoomStateCommandHandler getRoomStateHandler;

    @InjectMocks
    private SocketCommandDispatcher dispatcher;

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
    @CsvSource({"LEAVE_ROOM, HOST", "KICK_PLAYER, PLAYER", "CLOSE_ROOM, PLAYER"})
    void shouldThrowForbiddenWhenRoleIsNotAllowed(final String type, final RoleEnum role) {
        final SocketPrincipal principal = new SocketPrincipal(role, "participant-1", "ABC123");

        assertThatThrownBy(() -> dispatcher.dispatch(principal, new SocketCommand(type, null)))
                .isInstanceOf(SocketCommandException.class)
                .extracting(ex -> ((SocketCommandException) ex).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
        verifyNoInteractions(context);
    }
}
