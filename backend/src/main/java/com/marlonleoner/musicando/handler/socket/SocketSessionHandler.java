package com.marlonleoner.musicando.handler.socket;

import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.service.socket.PresenceService;
import com.marlonleoner.musicando.util.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Optional;

/**
 * Ciclo de vida das sessões WebSocket já autenticadas pelo {@link SocketAuthenticationInterceptor}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SocketSessionHandler {

    private final PresenceService presenceService;

    @EventListener
    public void handleConnected(SessionConnectedEvent event) {
        final String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
        principalOf(event).ifPresent(principal -> {
            try {
                presenceService.onConnected(principal, sessionId);
            } catch (ObjectNotFoundException ex) {
                log.warn("Connected participant {} not found", LogSanitizer.sanitize(principal.participantId()));
            }
        });
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        principalOf(event).ifPresent(principal -> {
            try {
                presenceService.onDisconnected(principal, event.getSessionId());
            } catch (ObjectNotFoundException ex) {
                log.warn("Disconnected participant {} not found", LogSanitizer.sanitize(principal.participantId()));
            }
        });
    }

    private Optional<SocketPrincipal> principalOf(AbstractSubProtocolEvent event) {
        return event.getUser() instanceof SocketPrincipal principal ? Optional.of(principal) : Optional.empty();
    }
}
