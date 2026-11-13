package com.marlonleoner.musicando.handler.socket;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.socket.SocketAttributes;
import com.marlonleoner.musicando.service.cache.RedisService;
import com.marlonleoner.musicando.service.match.MatchService;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.SocketService;
import com.marlonleoner.musicando.util.Constants;
import com.marlonleoner.musicando.util.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        log.info("Host disconnection initiated for room {} - SessionID: {}",
                LogSanitizer.sanitize(attributes.code()),
                LogSanitizer.sanitize(attributes.sessionId()));

        String code = attributes.code();

        Room room = roomService.getOrException(code);

        String redisKey = String.format(Constants.REDIS_HOST_CONNECTED, room.getCode());
        redisService.set(redisKey, false, Constants.SESSION_TTL_SECONDS);

        log.info("Host marked as disconnected for room {}", LogSanitizer.sanitize(room.getCode()));

        roomService.onDisconnect(room);
        matchService.onDisconnect(room.getCurrentMatch());

        // Notifica que o host saiu
        socketService.broadcastAll(null, code, redisKey);
    }
}
