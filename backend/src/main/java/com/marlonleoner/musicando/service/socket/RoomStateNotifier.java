package com.marlonleoner.musicando.service.socket;

import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.dto.socket.SocketEvent;
import com.marlonleoner.musicando.dto.socket.SocketEventReason;
import com.marlonleoner.musicando.mapper.RoomMapper;
import com.marlonleoner.musicando.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Monta e envia a foto completa da sala ({@code ROOM_STATE}). A sala é sempre recarregada do banco, para que o estado
 * enviado inclua a mudança que motivou o envio.
 */
@Service
@RequiredArgsConstructor
public class RoomStateNotifier {

    private final RoomService roomService;

    private final RoomMapper roomMapper;

    private final SocketService socketService;

    public void broadcast(String code, RoomStateReason reason, String participantId) throws ObjectNotFoundException {
        socketService.broadcastToRoom(code, buildEvent(code, reason, participantId));
    }

    public void sendTo(SocketPrincipal principal) throws ObjectNotFoundException {
        socketService.sendToParticipant(principal.participantId(), snapshot(principal));
    }

    public SocketEvent snapshot(SocketPrincipal principal) throws ObjectNotFoundException {
        return buildEvent(principal.roomCode(), RoomStateReason.SNAPSHOT, null);
    }

    private SocketEvent buildEvent(String code, RoomStateReason reason, String participantId) throws ObjectNotFoundException {
        return SocketEvent.roomState(roomMapper.toStateBroadcast(roomService.getOrException(code)), new SocketEventReason(reason, participantId));
    }
}
