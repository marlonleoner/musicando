package com.marlonleoner.musicando.dto.response;

/**
 * Retorno da entrada de um jogador na sala. O segredo é enviado apenas ao próprio jogador.
 */
public record JoinRoomResponse(String playerId, String secret, String roomCode) {
}
