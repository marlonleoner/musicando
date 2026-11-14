package com.marlonleoner.musicando.handler.socket.command;

import com.marlonleoner.musicando.domain.Match;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.ErrorCode;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.SocketCommandException;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.service.game.GameEngine;
import com.marlonleoner.musicando.service.match.MatchService;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code PLAY_AGAIN} (somente VIP, com a partida encerrada): todos voltam ao lobby com os pontos zerados e uma nova
 * partida já com a configuração da anterior.
 */
@Component
@RequiredArgsConstructor
public class PlayAgainCommandHandler implements SocketCommandHandler {

    private final PlayerService playerService;

    private final RoomService roomService;

    private final MatchService matchService;

    private final GameEngine gameEngine;

    private final RoomStateNotifier notifier;

    @Override
    public void handle(SocketPrincipal principal, JsonNode payload) throws BaseException {
        playerService.getOwnerOrException(principal.participantId());
        final Room room = roomService.getOrException(principal.roomCode());

        if (!room.isFinished()) {
            throw new SocketCommandException(ErrorCode.INVALID_COMMAND, "The game is not finished");
        }

        gameEngine.stop(principal.roomCode());
        final Match match = matchService.createNext(room, room.getCurrentMatch());
        roomService.attachMatch(room, match);
        playerService.resetResults(room.getPlayers());
        roomService.reopen(room);
        notifier.broadcast(principal.roomCode(), RoomStateReason.PLAY_AGAIN, principal.participantId());
    }
}
