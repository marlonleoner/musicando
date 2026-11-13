package com.marlonleoner.musicando.controller;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.SocketCommandException;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.dto.socket.SocketCommand;
import com.marlonleoner.musicando.dto.socket.SocketErrorPayload;
import com.marlonleoner.musicando.dto.socket.SocketEvent;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import com.marlonleoner.musicando.service.socket.SocketCommandDispatcher;
import com.marlonleoner.musicando.service.socket.SocketService;
import com.marlonleoner.musicando.util.SocketDestinations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Recebe os comandos dos clientes. Erros são devolvidos apenas ao remetente, em {@code /user/queue/errors}.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class SocketController {

    private static final String INTERNAL_ERROR_MESSAGE = "Internal server error";

    private final SocketCommandDispatcher dispatcher;

    private final SocketService socketService;

    private final RoomStateNotifier notifier;

    @MessageMapping(SocketDestinations.COMMAND_MAPPING)
    public void handleCommand(@Payload SocketCommand command, Principal principal) throws BaseException {
        dispatcher.dispatch((SocketPrincipal) principal, command);
    }

    /**
     * Foto inicial da sala, respondida diretamente a quem se inscreveu em {@code /musicando/room}.
     */
    @SubscribeMapping(SocketDestinations.ROOM_SNAPSHOT_MAPPING)
    public String handleRoomSnapshot(Principal principal) throws BaseException {
        return socketService.toJson(notifier.snapshot((SocketPrincipal) principal));
    }

    @MessageExceptionHandler(SocketCommandException.class)
    public void handleCommandException(SocketCommandException ex, Principal principal) {
        sendError(principal, ex.getErrorCode(), ex.getMessage());
    }

    @MessageExceptionHandler(BaseException.class)
    public void handleBaseException(BaseException ex, Principal principal) {
        sendError(principal, ex.getErrorCode(), ex.getMessage());
    }

    @MessageExceptionHandler(Exception.class)
    public void handleUnexpectedException(Exception ex, Principal principal) {
        log.error("Unexpected error while processing socket command", ex);
        sendError(principal, ErrorCode.INTERNAL_ERROR, INTERNAL_ERROR_MESSAGE);
    }

    private void sendError(Principal principal, ErrorCode code, String message) {
        socketService.sendErrorToParticipant(principal.getName(), SocketEvent.error(new SocketErrorPayload(code, message, null)));
    }
}
