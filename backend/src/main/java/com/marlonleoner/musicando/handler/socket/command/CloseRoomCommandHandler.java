package com.marlonleoner.musicando.handler.socket.command;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code CLOSE_ROOM} (somente host): encerra a sala. Todos recebem o {@code ROOM_STATE} com status {@code CLOSED} e
 * devem voltar para a tela inicial.
 */
@Component
@RequiredArgsConstructor
public class CloseRoomCommandHandler implements SocketCommandHandler {

    private final RoomService roomService;

    private final RoomStateNotifier notifier;

    @Override
    public void handle(SocketPrincipal principal, JsonNode payload) throws BaseException {
        final Room room = roomService.getOrException(principal.roomCode());
        roomService.close(room);
        notifier.broadcast(principal.roomCode(), RoomStateReason.ROOM_CLOSED, principal.participantId());
    }
}
