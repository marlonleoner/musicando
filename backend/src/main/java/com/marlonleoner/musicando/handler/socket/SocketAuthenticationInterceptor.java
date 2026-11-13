package com.marlonleoner.musicando.handler.socket;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import com.marlonleoner.musicando.domain.exception.SocketAuthenticationException;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.service.socket.SocketAuthenticationService;
import com.marlonleoner.musicando.util.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Valida cada frame STOMP recebido antes de ele ser processado:
 * <ul>
 *     <li>{@code CONNECT}: autentica as credenciais e define a identidade da conexão;</li>
 *     <li>{@code SUBSCRIBE}: permite apenas os destinos da própria sala e as filas privadas;</li>
 *     <li>{@code SEND}: permite apenas o endpoint de comandos.</li>
 * </ul>
 * Uma falha gera um frame {@code ERROR} com o código do erro e encerra a conexão.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SocketAuthenticationInterceptor implements ChannelInterceptor {

    private static final String ROLE_HEADER = "role";

    private static final String CODE_HEADER = "code";

    private static final String ID_HEADER = "id";

    private static final String SECRET_HEADER = "secret";

    private final SocketAuthenticationService authenticationService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (Objects.isNull(accessor) || Objects.isNull(accessor.getCommand())) {
            return message;
        }

        final StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            accessor.setUser(authenticationService.authenticate(
                    accessor.getFirstNativeHeader(ROLE_HEADER),
                    accessor.getFirstNativeHeader(CODE_HEADER),
                    accessor.getFirstNativeHeader(ID_HEADER),
                    accessor.getFirstNativeHeader(SECRET_HEADER)));
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            authenticationService.authorizeSubscription(requirePrincipal(accessor), accessor.getDestination());
        } else if (StompCommand.SEND.equals(command)) {
            requirePrincipal(accessor);
            authenticationService.authorizeSend(accessor.getDestination());
        }

        return message;
    }

    private SocketPrincipal requirePrincipal(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof SocketPrincipal principal) {
            return principal;
        }

        log.warn("Frame received without authenticated principal - session {}", LogSanitizer.sanitize(accessor.getSessionId()));
        throw new SocketAuthenticationException(ErrorCode.FORBIDDEN);
    }
}
