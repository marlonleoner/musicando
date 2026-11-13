package com.marlonleoner.musicando.handler.socket.command;

import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GetRoomStateCommandHandlerTest {

    @Mock
    private RoomStateNotifier notifier;

    @InjectMocks
    private GetRoomStateCommandHandler handler;

    @Test
    void shouldSendRoomStateToSenderWhenHandlingCommand() throws BaseException {
        final SocketPrincipal principal = new SocketPrincipal(RoleEnum.PLAYER, "player-1", "ABC123");

        handler.handle(principal, null);

        verify(notifier).sendTo(principal);
    }
}
