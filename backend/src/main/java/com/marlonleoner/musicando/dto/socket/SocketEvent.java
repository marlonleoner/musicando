package com.marlonleoner.musicando.dto.socket;

import com.marlonleoner.musicando.domain.socket.SocketEventType;
import com.marlonleoner.musicando.dto.response.RoomStateBroadcast;

import java.time.Instant;

/**
 * Envelope de todos os eventos enviados pelo servidor aos clientes.
 *
 * @param type      tipo do evento
 * @param reason    motivo do envio (opcional, apenas para avisos na interface)
 * @param payload   dados do evento
 * @param timestamp momento de criação do evento
 */
public record SocketEvent(SocketEventType type, SocketEventReason reason, Object payload, Instant timestamp) {
    public static SocketEvent roomState(final RoomStateBroadcast state, final SocketEventReason reason) {
        return new SocketEvent(SocketEventType.ROOM_STATE, reason, state, Instant.now());
    }

    public static SocketEvent kicked() {
        return new SocketEvent(SocketEventType.KICKED, null, null, Instant.now());
    }

    public static SocketEvent error(final SocketErrorPayload error) {
        return new SocketEvent(SocketEventType.ERROR, null, error, Instant.now());
    }
}
