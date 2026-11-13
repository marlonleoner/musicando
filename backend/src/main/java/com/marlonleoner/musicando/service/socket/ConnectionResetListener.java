package com.marlonleoner.musicando.service.socket;

import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Ao iniciar a aplicação nenhuma conexão WebSocket está ativa. Limpa as marcações de conexão que possam ter ficado
 * para trás caso o backend tenha sido encerrado sem processar as desconexões.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionResetListener {

    private final RoomService roomService;

    private final PlayerService playerService;

    @EventListener(ApplicationReadyEvent.class)
    public void resetConnections() {
        final int rooms = roomService.resetConnections();
        final int players = playerService.resetConnections();
        log.info("Stale connections reset: {} room(s) and {} player(s)", rooms, players);
    }
}
