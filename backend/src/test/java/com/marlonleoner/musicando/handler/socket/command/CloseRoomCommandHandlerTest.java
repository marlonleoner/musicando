package com.marlonleoner.musicando.handler.socket.command;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseRoomCommandHandlerTest {

    @Mock
    private RoomService roomService;

    @Mock
    private RoomStateNotifier notifier;

    @InjectMocks
    private CloseRoomCommandHandler handler;

    @Test
    void shouldCloseRoomAndNotifyEveryoneWhenHostClosesRoom() throws BaseException {
        final Room room = Fixture.make(Room.class);
        final SocketPrincipal principal = new SocketPrincipal(RoleEnum.HOST, room.getId(), room.getCode());
        when(roomService.getOrException(room.getCode())).thenReturn(room);

        handler.handle(principal, null);

        final InOrder order = inOrder(roomService, notifier);
        order.verify(roomService).close(room);
        order.verify(notifier).broadcast(room.getCode(), RoomStateReason.ROOM_CLOSED, room.getId());
    }
}
