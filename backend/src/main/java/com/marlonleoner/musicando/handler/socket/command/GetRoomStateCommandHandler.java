package com.marlonleoner.musicando.handler.socket.command;

import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code GET_ROOM_STATE}: reenvia a foto completa da sala apenas ao remetente.
 */
@Component
@RequiredArgsConstructor
public class GetRoomStateCommandHandler implements SocketCommandHandler {

    private final RoomStateNotifier notifier;

    @Override
    public void handle(SocketPrincipal principal, JsonNode payload) throws BaseException {
        notifier.sendTo(principal);
    }
}
