package com.marlonleoner.musicando.handler.socket;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.socket.SocketAttributes;
import com.marlonleoner.musicando.service.cache.RedisService;
import com.marlonleoner.musicando.service.match.MatchService;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.util.Constants;
import com.marlonleoner.musicando.util.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class HostConnectHandler extends AbstractSocketHandler {

    private final RedisService redisService;

    private final RoomService roomService;

    private final MatchService matchService;

    @Override
    protected void handle(SocketAttributes attributes) throws BaseException {
        log.info("Host connection attempt initiated for room {} - SessionID: {}",
                LogSanitizer.sanitize(attributes.code()),
                LogSanitizer.sanitize(attributes.sessionId()));

        String code = attributes.code();

        Room room = roomService.getOrException(code);

        if (!room.validateSecrets(attributes.id(), attributes.secret())) {
            log.warn("Connection failed for room {}: Invalid keys provided", LogSanitizer.sanitize(code));
            throw new BaseException("Connection failed: Invalid keys provided");
        }

        String redisKey = String.format(Constants.REDIS_HOST_CONNECTED, room.getCode());
        redisService.set(redisKey, true, Constants.SESSION_TTL_SECONDS);

        log.debug("Host marked as connected for room {}", LogSanitizer.sanitize(room.getCode()));

        roomService.onConnect(room);
        matchService.onConnect(room.getCurrentMatch());
    }
}
