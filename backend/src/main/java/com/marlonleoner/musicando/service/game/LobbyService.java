package com.marlonleoner.musicando.service.game;

import com.marlonleoner.musicando.domain.Match;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.service.match.MatchService;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Leva uma sala de volta ao lobby: descarta a partida atual e cria uma nova com a mesma configuração, com os pontos
 * dos jogadores zerados.
 */
@Service
@RequiredArgsConstructor
public class LobbyService {

    private final RoomService roomService;

    private final MatchService matchService;

    private final PlayerService playerService;

    private final GameEngine gameEngine;

    public void backToLobby(Room room) {
        final Match previous = room.getCurrentMatch();
        gameEngine.stop(room.getCode());
        matchService.finish(previous);
        roomService.attachMatch(room, matchService.createNext(room, previous));
        playerService.resetResults(room.getPlayers());
        roomService.reopen(room);
    }
}
