package com.marlonleoner.musicando.config;

import com.marlonleoner.musicando.config.properties.CorsProperties;
import com.marlonleoner.musicando.handler.socket.SocketAuthenticationInterceptor;
import com.marlonleoner.musicando.handler.socket.SocketErrorHandler;
import com.marlonleoner.musicando.handler.socket.SocketSessionRegistry;
import com.marlonleoner.musicando.util.SocketDestinations;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * WebSocket com STOMP (sem SockJS).
 *
 * <ul>
 *     <li>Endpoint: {@code /ws}, aceitando apenas as origens de {@code musicando.cors.allowed-origins};</li>
 *     <li>Comandos dos clientes: {@code /musicando/command};</li>
 *     <li>Foto inicial da sala: inscrição em {@code /musicando/room}, respondida apenas a quem se inscreveu;</li>
 *     <li>Eventos do servidor: {@code /topic/room/{code}} (sala), {@code /topic/room/{code}/host} (host) e
 *     {@code /user/queue/events} / {@code /user/queue/errors} (participante).</li>
 * </ul>
 * A ordem das mensagens enviadas a cada cliente é preservada, para que ele receba os eventos na mesma ordem em que
 * foram gerados. A ordem de recebimento não é forçada: com ela, falhas de autenticação no {@code CONNECT} e no
 * {@code SUBSCRIBE} deixariam de gerar o frame {@code ERROR} para o cliente.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class SocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private final CorsProperties corsProperties;

    private final SocketAuthenticationInterceptor authenticationInterceptor;

    private final SocketSessionRegistry sessionRegistry;

    private final SocketErrorHandler errorHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setUserDestinationPrefix(SocketDestinations.USER_PREFIX);
        config.setApplicationDestinationPrefixes(SocketDestinations.APPLICATION_PREFIX);
        config.setPreservePublishOrder(true);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(this.corsProperties.allowedOriginsAsArray());
        registry.setErrorHandler(this.errorHandler);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(this.authenticationInterceptor);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(this.sessionRegistry);
    }
}
