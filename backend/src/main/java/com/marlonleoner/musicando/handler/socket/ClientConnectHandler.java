package com.marlonleoner.musicando.handler.socket;

import com.marlonleoner.musicando.domain.socket.SocketAttributes;
import com.marlonleoner.musicando.util.LogSanitizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClientConnectHandler extends AbstractSocketHandler {

    @Override
    protected void handle(SocketAttributes attributes) {
        log.info("Client connected: {}", LogSanitizer.sanitize(attributes.sessionId()));
    }
}
