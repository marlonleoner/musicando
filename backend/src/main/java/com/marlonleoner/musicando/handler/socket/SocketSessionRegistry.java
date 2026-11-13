package com.marlonleoner.musicando.handler.socket;

import com.marlonleoner.musicando.util.LogSanitizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mantém as sessões WebSocket abertas nesta instância, permitindo encerrar a conexão anterior quando o mesmo
 * participante se conecta novamente.
 */
@Slf4j
@Component
public class SocketSessionRegistry implements WebSocketHandlerDecoratorFactory {

    /**
     * Código de fechamento enviado à conexão substituída por uma mais recente (faixa 4000-4999, livre para aplicações).
     */
    public static final CloseStatus SESSION_REPLACED = new CloseStatus(4001, "SESSION_REPLACED");

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public WebSocketHandler decorate(WebSocketHandler handler) {
        return new WebSocketHandlerDecorator(handler) {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                sessions.put(session.getId(), session);
                super.afterConnectionEstablished(session);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                sessions.remove(session.getId());
                super.afterConnectionClosed(session, closeStatus);
            }
        };
    }

    public void closeReplaced(String sessionId) {
        final WebSocketSession session = sessions.get(sessionId);

        if (Objects.isNull(session)) {
            return;
        }

        try {
            session.close(SESSION_REPLACED);
        } catch (IOException ex) {
            log.warn("Could not close replaced session {}: {}", LogSanitizer.sanitize(sessionId), LogSanitizer.sanitize(ex.getMessage()));
        }
    }
}
