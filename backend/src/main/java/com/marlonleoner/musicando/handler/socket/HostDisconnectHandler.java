package com.marlonleoner.musicando.handler.socket;

import org.springframework.stereotype.Component;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.socket.SocketAttributes;
import com.marlonleoner.musicando.service.MatchService;
import com.marlonleoner.musicando.service.RedisService;
import com.marlonleoner.musicando.service.RoomService;
import com.marlonleoner.musicando.service.SocketService;
import com.marlonleoner.musicando.util.Constants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class HostDisconnectHandler extends AbstractSocketHandler {

    private final RedisService redisService;

    private final SocketService socketService;

    private final RoomService roomService;

    private final MatchService matchService;

    @Override
    protected void handle(SocketAttributes attributes) throws BaseException {
        log.info("Host connection attempt initiated for room {} - SessionID: {}",
                attributes.code(),
                attributes.sessionId());

        String code = attributes.code();

        Room room = roomService.getOrException(code);

        String redisKey = String.format(Constants.REDIS_HOST_CONNECTED, room.getCode());
        redisService.set(redisKey, false, 3600);

        log.info("Host marked as disconnected for room {}", room.getCode());

        roomService.onDisconnect(room);
        matchService.onDisconnect(room.getCurrentMatch());

        // Notifica que o host saiu
        socketService.broadcastAll(null, code, redisKey);
    }
}
