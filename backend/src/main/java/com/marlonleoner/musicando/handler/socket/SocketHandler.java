package com.marlonleoner.musicando.handler.socket;

import java.util.Objects;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.marlonleoner.musicando.domain.socket.ConnectionTypeEnum;
import com.marlonleoner.musicando.domain.socket.SocketAttributes;
import com.marlonleoner.musicando.service.RedisService;
import com.marlonleoner.musicando.util.Constants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class SocketHandler {

    private final RedisService redisService;

    private final ApplicationContext context;

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        SocketAttributes attributes = SocketAttributes.fromHeaders(accessor);

        String key = getSessionRedisKey(attributes.sessionId());
        redisService.set(key, attributes, 3600);

        execute(attributes, true);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String key = getSessionRedisKey(accessor.getSessionId());
        redisService.get(key, SocketAttributes.class).ifPresent(attributes -> {
            redisService.invalidate(key);

            execute(attributes, false);
        });
    }

    private String getSessionRedisKey(String sessionId) {
        return String.format(Constants.REDIS_SESSIONS, sessionId);
    }

    private void execute(SocketAttributes connection, boolean connect) {
        try {
            ConnectionTypeEnum type = ConnectionTypeEnum.get(connection.role(), connect);
            if (Objects.nonNull(type)) {
                AbstractSocketHandler handler = context.getBean(type.getHandler());
                handler.handle(connection);
            }
        } catch (Exception ex) {
            log.error("Fail SocketHandler execute: {}", ex.getMessage());
        }
    }
}
