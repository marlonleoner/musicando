package com.marlonleoner.musicando.domain.socket;

import com.marlonleoner.musicando.domain.enums.RoleEnum;

import java.security.Principal;

/**
 * Identidade de uma conexão WebSocket autenticada. É definida na validação do {@code CONNECT} e acompanha todas as
 * mensagens da sessão até a desconexão.
 *
 * @param role          papel do participante (host ou jogador)
 * @param participantId id da sala (host) ou do jogador
 * @param roomCode      código da sala
 */
public record SocketPrincipal(RoleEnum role, String participantId, String roomCode) implements Principal {

    @Override
    public String getName() {
        return this.participantId;
    }

    public boolean isHost() {
        return RoleEnum.HOST.equals(this.role);
    }
}
