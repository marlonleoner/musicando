package com.marlonleoner.musicando.service.socket;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.handler.socket.SocketSessionRegistry;
import com.marlonleoner.musicando.service.match.MatchService;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.util.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Presença dos participantes: marca host e jogadores como conectados ou desconectados, mantém apenas a conexão mais
 * recente de cada participante e avisa a sala a cada mudança.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceService {

    private final RoomService roomService;

    private final PlayerService playerService;

    private final MatchService matchService;

    private final RoomStateNotifier notifier;

    private final SocketSessionRegistry sessionRegistry;

    public void onConnected(SocketPrincipal principal, String sessionId) throws ObjectNotFoundException {
        if (principal.isHost()) {
            final Room room = roomService.getOrException(principal.roomCode());
            closePreviousSession(room.getHostSessionId(), sessionId);
            roomService.onConnect(room, sessionId);
            matchService.onConnect(room.getCurrentMatch());
            notifier.broadcast(principal.roomCode(), RoomStateReason.HOST_CONNECTED, principal.participantId());

            return;
        }

        final Player player = playerService.getOrException(principal.participantId());
        closePreviousSession(player.getSessionId(), sessionId);
        playerService.onConnect(player, sessionId);
        notifier.broadcast(principal.roomCode(), RoomStateReason.PLAYER_CONNECTED, principal.participantId());
    }

    /**
     * Trata a desconexão. Sessões já substituídas por uma conexão mais recente do mesmo participante são ignoradas.
     */
    public void onDisconnected(SocketPrincipal principal, String sessionId) throws ObjectNotFoundException {
        if (principal.isHost()) {
            final Room room = roomService.getOrException(principal.roomCode());

            if (!sessionId.equals(room.getHostSessionId())) {
                log.debug("Ignoring disconnection of replaced host session {}", LogSanitizer.sanitize(sessionId));

                return;
            }

            roomService.onDisconnect(room);
            matchService.onDisconnect(room.getCurrentMatch());
            notifier.broadcast(principal.roomCode(), RoomStateReason.HOST_DISCONNECTED, principal.participantId());

            return;
        }

        final Player player = playerService.getOrException(principal.participantId());

        if (!sessionId.equals(player.getSessionId())) {
            log.debug("Ignoring disconnection of replaced player session {}", LogSanitizer.sanitize(sessionId));

            return;
        }

        playerService.onDisconnect(player);
        notifier.broadcast(principal.roomCode(), RoomStateReason.PLAYER_DISCONNECTED, principal.participantId());
    }

    private void closePreviousSession(String previousSessionId, String currentSessionId) {
        if (Objects.nonNull(previousSessionId) && !previousSessionId.equals(currentSessionId)) {
            sessionRegistry.closeReplaced(previousSessionId);
        }
    }
}
