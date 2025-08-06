package com.marlonleoner.musicando.handler.socket;

import org.springframework.stereotype.Component;

import com.marlonleoner.musicando.domain.socket.SocketAttributes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ClientDisconnectHandler extends AbstractSocketHandler {

    @Override
    protected void handle(SocketAttributes attributes) {
        log.info("Client disconnected: {}", attributes.sessionId());
    }
}
