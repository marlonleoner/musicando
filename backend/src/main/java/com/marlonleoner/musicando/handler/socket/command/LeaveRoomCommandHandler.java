package com.marlonleoner.musicando.handler.socket.command;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code LEAVE_ROOM}: o jogador sai da sala de vez. Se era o VIP, a coroa passa para outro jogador. Após enviar o
 * comando, o cliente deve encerrar a conexão.
 */
@Component
@RequiredArgsConstructor
public class LeaveRoomCommandHandler implements SocketCommandHandler {

    private final PlayerService playerService;

    private final RoomStateNotifier notifier;

    @Override
    public void handle(SocketPrincipal principal, JsonNode payload) throws BaseException {
        final Player player = playerService.getOrException(principal.participantId());
        playerService.delete(player);
        playerService.ensureOwner(principal.roomCode());
        notifier.broadcast(principal.roomCode(), RoomStateReason.PLAYER_LEFT, principal.participantId());
    }
}
