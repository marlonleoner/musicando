package com.marlonleoner.musicando.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class SocketConfiguration implements WebSocketMessageBrokerConfigurer {

    /**
     * Configura o broker de mensagens para comunicação via WebSocket.
     * 
     * Este método define:
     * - Canais de broadcast '/topic' e '/queue' para envio de mensagens para
     * múltiplos clientes
     * - O prefixo '/musicando' que deve ser usado para mensagens enviadas para os
     * controladores
     * destinados a processamento pelo servidor
     *
     * @param config Registro do message broker usado para configuração
     */
    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/musicando");
    }

    /**
     * Configura o endpoint WebSocket para comunicação em tempo real.
     * 
     * Este método define:
     * - O endpoint '/ws' onde os clientes podem se conectar via WebSocket
     * - Permite conexões de todas as origens (*) para facilitar o desenvolvimento
     * - Habilita o suporte a SockJS como fallback para navegadores que não
     * suportam WebSockets nativamente ou em ambientes onde WebSockets são
     * bloqueados
     *
     * @param registry Registro de endpoints STOMP usado para configuração
     */
    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setInterceptors(new HttpSessionHandshakeInterceptor());
    }
}