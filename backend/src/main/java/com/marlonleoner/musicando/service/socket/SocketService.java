package com.marlonleoner.musicando.service.socket;

import com.marlonleoner.musicando.dto.socket.SocketEvent;
import com.marlonleoner.musicando.util.LogSanitizer;
import com.marlonleoner.musicando.util.SocketDestinations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * Envio de eventos aos clientes conectados.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SocketService {

    private final SimpMessagingTemplate messagingTemplate;

    private final JsonMapper jsonMapper;

    /**
     * Envia o evento a todos os participantes da sala (host e jogadores).
     */
    public void broadcastToRoom(String code, SocketEvent event) {
        send(SocketDestinations.roomTopic(code), event);
    }

    /**
     * Envia o evento apenas ao host da sala.
     */
    public void sendToHost(String code, SocketEvent event) {
        send(SocketDestinations.roomHostTopic(code), event);
    }

    /**
     * Envia o evento apenas a um participante (host ou jogador), em {@code /user/queue/events}.
     */
    public void sendToParticipant(String participantId, SocketEvent event) {
        final String message = jsonMapper.writeValueAsString(event);
        log.debug("Sending {} to participant {}", LogSanitizer.sanitize(event.type()), LogSanitizer.sanitize(participantId));
        messagingTemplate.convertAndSendToUser(participantId, SocketDestinations.EVENTS_QUEUE, message);
    }

    /**
     * Envia um erro apenas a um participante (host ou jogador), em {@code /user/queue/errors}.
     */
    public void sendErrorToParticipant(String participantId, SocketEvent event) {
        final String message = jsonMapper.writeValueAsString(event);
        log.debug("Sending error to participant {}", LogSanitizer.sanitize(participantId));
        messagingTemplate.convertAndSendToUser(participantId, SocketDestinations.ERRORS_QUEUE, message);
    }

    public String toJson(SocketEvent event) {
        return jsonMapper.writeValueAsString(event);
    }

    private void send(String destination, SocketEvent event) {
        final String message = jsonMapper.writeValueAsString(event);
        log.debug("Sending {} to {}", LogSanitizer.sanitize(event.type()), LogSanitizer.sanitize(destination));
        messagingTemplate.convertAndSend(destination, message);
    }
}
