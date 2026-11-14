package com.marlonleoner.musicando.service.game;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.service.match.MatchService;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.util.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

/**
 * Na inicialização, retoma as partidas interrompidas por um reinício do backend. Com o estado da partida ainda no
 * Redis, a sala fica pausada até a TV reconectar e a partida segue de onde parou; sem ele, a sala volta para o lobby.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GameRecoveryListener {

    private final RoomService roomService;

    private final MatchService matchService;

    private final GameEngine gameEngine;

    private final LobbyService lobbyService;

    @EventListener(ApplicationReadyEvent.class)
    public void recoverInterruptedGames() {
        roomService.findInterrupted().forEach(this::recover);
    }

    private void recover(Room room) {
        final String code = LogSanitizer.sanitize(room.getCode());

        try {
            if (gameEngine.recover(room.getCode())) {
                roomService.pause(room);
                matchService.onDisconnect(room.getCurrentMatch());
                log.info("Game in room {} paused until the TV reconnects", code);

                return;
            }

            lobbyService.backToLobby(room);
            log.info("Game state of room {} was lost; room sent back to the lobby", code);
        } catch (DataAccessException ex) {
            log.warn("Could not recover the game in room {}: {}", code, LogSanitizer.sanitize(ex.getMessage()));
        }
    }
}
