package com.marlonleoner.musicando.handler.socket;

import com.marlonleoner.musicando.domain.socket.SocketAttributes;
import com.marlonleoner.musicando.util.LogSanitizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClientDisconnectHandler extends AbstractSocketHandler {

    @Override
    protected void handle(SocketAttributes attributes) {
        log.info("Client disconnected: {}", LogSanitizer.sanitize(attributes.sessionId()));
    }
}
