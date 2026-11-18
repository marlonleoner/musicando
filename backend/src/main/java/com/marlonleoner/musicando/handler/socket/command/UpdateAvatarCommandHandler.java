package com.marlonleoner.musicando.handler.socket.command;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.exception.AvatarTakenException;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.GameAlreadyStartedException;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.service.game.GameLocks;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import com.marlonleoner.musicando.validator.AvatarValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.Objects;

/**
 * {@code UPDATE_AVATAR} (jogador, com a sala aguardando jogadores). Payload: {@code { "avatar": "robot" }}. O personagem
 * precisa estar livre na sala; a verificação acontece sob o lock da sala, para duas trocas simultâneas não pegarem o
 * mesmo personagem. Todos na sala recebem o {@code ROOM_STATE} com a troca.
 */
@Component
@RequiredArgsConstructor
public class UpdateAvatarCommandHandler implements SocketCommandHandler {

    private static final String AVATAR_FIELD = "avatar";

    private final PlayerService playerService;

    private final RoomService roomService;

    private final RoomStateNotifier notifier;

    private final GameLocks locks;

    @Override
    public void handle(SocketPrincipal principal, JsonNode payload) throws BaseException {
        final String avatar = Objects.isNull(payload) ? "" : payload.path(AVATAR_FIELD).asString("");
        AvatarValidator.validate(avatar);
        final Room room = roomService.getOrException(principal.roomCode());

        if (!room.isWaiting()) {
            throw new GameAlreadyStartedException(room.getCode());
        }

        final Player player = playerService.getOrException(principal.participantId());

        locks.run(room.getCode(), () -> {
            final boolean taken = playerService.findByRoom(room.getCode()).stream()
                    .anyMatch(other -> !other.getId().equals(player.getId()) && avatar.equals(other.getAvatar()));

            if (taken) {
                throw new AvatarTakenException(avatar);
            }

            playerService.updateAvatar(player, avatar);
        });

        notifier.broadcast(room.getCode(), RoomStateReason.AVATAR_UPDATED, principal.participantId());
    }
}
