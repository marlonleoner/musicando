package com.marlonleoner.musicando.handler.socket;

import org.springframework.stereotype.Component;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.socket.SocketAttributes;
import com.marlonleoner.musicando.service.RoomService;
import com.marlonleoner.musicando.service.SocketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class HostConnectHandler extends AbstractSocketHandler {

    private final SocketService socketService;

    private final RoomService roomService;

    @Override
    protected void handle(SocketAttributes attributes) throws BaseException {
        log.info("Host connected: {}", attributes.sessionId());

        Room room = roomService.getOrException(attributes.code());
        if (!room.validateSecret(attributes.secret())) {
            log.warn("Invalid secret for room: {}", attributes.code());
            throw new BaseException("Invalid secret for room");
        }

    }
}
