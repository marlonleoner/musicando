package com.marlonleoner.musicando.handler.socket;

import org.springframework.stereotype.Component;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.socket.SocketAttributes;
import com.marlonleoner.musicando.service.MatchService;
import com.marlonleoner.musicando.service.RedisService;
import com.marlonleoner.musicando.service.RoomService;
import com.marlonleoner.musicando.util.Constants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
                attributes.code(),
                attributes.sessionId());

        String code = attributes.code();

        Room room = roomService.getOrException(code);
        if (!room.validateSecrets(attributes.id(), attributes.secret())) {
            log.warn("Connection failed for room {}: Invalid keys provided", code);
            throw new BaseException("Connection failed: Invalid keys provided");
        }

        String redisKey = String.format(Constants.REDIS_HOST_CONNECTED, room.getCode());
        redisService.set(redisKey, true, 3600);

        log.debug("Host marked as connected for room {}", room.getCode());

        roomService.onConnect(room);
        matchService.onConnect(room.getCurrentMatch());
    }
}
