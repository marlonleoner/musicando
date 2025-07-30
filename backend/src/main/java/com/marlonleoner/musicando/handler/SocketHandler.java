package com.marlonleoner.musicando.handler;

import java.util.List;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.marlonleoner.musicando.domain.dto.SocketConnectionDTO;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SocketHandler {

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        log.info("🟢 Conexão detectada");

        Message<?> message = (Message<?>) event.getMessage().getHeaders().get("simpConnectMessage");
        Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) message.getHeaders().get("nativeHeaders");

        SocketConnectionDTO connection = SocketConnectionDTO.fromNativeHeaders(nativeHeaders);

        log.info("connection {}", connection);

    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        log.info("🔴 Desconexão detectada");
    }
}