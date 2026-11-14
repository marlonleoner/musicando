package com.marlonleoner.musicando.handler.socket.command;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.enums.ErrorCode;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.domain.exception.SocketCommandException;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.dto.socket.SocketEvent;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import com.marlonleoner.musicando.service.socket.SocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.Objects;

/**
 * {@code KICK_PLAYER} (somente host): remove um jogador da sala. Payload: {@code { "playerId": "..." }}. O jogador
 * removido recebe o evento {@code KICKED} em sua fila privada e deve encerrar a conexão.
 */
@Component
@RequiredArgsConstructor
public class KickPlayerCommandHandler implements SocketCommandHandler {

    private static final String PLAYER_ID_FIELD = "playerId";

    private final PlayerService playerService;

    private final SocketService socketService;

    private final RoomStateNotifier notifier;

    @Override
    public void handle(SocketPrincipal principal, JsonNode payload) throws BaseException {
        final String playerId = Objects.isNull(payload) ? "" : payload.path(PLAYER_ID_FIELD).asString("");

        if (playerId.isBlank()) {
            throw new SocketCommandException(ErrorCode.INVALID_COMMAND, "Field playerId is required");
        }

        final Player player = playerService.getOrException(playerId);

        if (Objects.isNull(player.getRoom()) || !principal.roomCode().equals(player.getRoom().getCode())) {
            throw new ObjectNotFoundException(Player.class);
        }

        socketService.sendToParticipant(playerId, SocketEvent.kicked());
        playerService.delete(player);
        playerService.ensureOwner(principal.roomCode());
        notifier.broadcast(principal.roomCode(), RoomStateReason.PLAYER_KICKED, playerId);
    }
}
