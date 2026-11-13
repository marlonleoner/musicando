package com.marlonleoner.musicando.controller;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.enums.RoomStatus;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.domain.exception.RoomNotJoinableException;
import com.marlonleoner.musicando.domain.exception.SocketCommandException;
import com.marlonleoner.musicando.domain.socket.SocketErrorCode;
import com.marlonleoner.musicando.domain.socket.SocketEventType;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.dto.socket.SocketCommand;
import com.marlonleoner.musicando.dto.socket.SocketErrorPayload;
import com.marlonleoner.musicando.dto.socket.SocketEvent;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import com.marlonleoner.musicando.service.socket.SocketCommandDispatcher;
import com.marlonleoner.musicando.service.socket.SocketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocketControllerTest {

    private static final SocketPrincipal PRINCIPAL = new SocketPrincipal(RoleEnum.HOST, "room-id", "ABC123");

    @Mock
    private SocketCommandDispatcher dispatcher;

    @Mock
    private SocketService socketService;

    @Mock
    private RoomStateNotifier notifier;

    @InjectMocks
    private SocketController controller;

    @Test
    void shouldDispatchCommandWithPrincipalWhenCommandIsReceived() throws BaseException {
        final SocketCommand command = new SocketCommand("GET_ROOM_STATE", null);

        controller.handleCommand(command, PRINCIPAL);

        verify(dispatcher).dispatch(PRINCIPAL, command);
    }

    @Test
    void shouldReturnSerializedSnapshotWhenSubscribingToRoomSnapshot() throws BaseException {
        final SocketEvent snapshot = SocketEvent.roomState(null, null);
        when(notifier.snapshot(PRINCIPAL)).thenReturn(snapshot);
        when(socketService.toJson(snapshot)).thenReturn("{}");

        assertThat(controller.handleRoomSnapshot(PRINCIPAL)).isEqualTo("{}");
    }

    @Test
    void shouldSendErrorWithCommandCodeToSenderWhenCommandIsRejected() {
        controller.handleCommandException(new SocketCommandException(SocketErrorCode.FORBIDDEN, "denied"), PRINCIPAL);

        assertErrorSent(SocketErrorCode.FORBIDDEN);
    }

    @Test
    void shouldSendRoomNotFoundToSenderWhenObjectIsNotFound() {
        controller.handleBaseException(new ObjectNotFoundException(Room.class), PRINCIPAL);

        assertErrorSent(SocketErrorCode.ROOM_NOT_FOUND);
    }

    @Test
    void shouldSendRoomNotJoinableToSenderWhenRoomIsNotJoinable() {
        controller.handleBaseException(new RoomNotJoinableException("ABC123", RoomStatus.FINISHED), PRINCIPAL);

        assertErrorSent(SocketErrorCode.ROOM_NOT_JOINABLE);
    }

    @Test
    void shouldSendInternalErrorToSenderWhenBaseExceptionIsGeneric() {
        controller.handleBaseException(new BaseException("generic"), PRINCIPAL);

        assertErrorSent(SocketErrorCode.INTERNAL_ERROR);
    }

    @Test
    void shouldHideMessageWhenUnexpectedErrorOccurs() {
        controller.handleUnexpectedException(new IllegalStateException("database password is 123"), PRINCIPAL);

        final SocketErrorPayload error = assertErrorSent(SocketErrorCode.INTERNAL_ERROR);
        assertThat(error.message()).doesNotContain("password");
    }

    private SocketErrorPayload assertErrorSent(final SocketErrorCode code) {
        final ArgumentCaptor<SocketEvent> captor = ArgumentCaptor.forClass(SocketEvent.class);
        verify(socketService).sendErrorToParticipant(eq(PRINCIPAL.participantId()), captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(SocketEventType.ERROR);
        final SocketErrorPayload error = (SocketErrorPayload) captor.getValue().payload();
        assertThat(error.code()).isEqualTo(code);

        return error;
    }
}
