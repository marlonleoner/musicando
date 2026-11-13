package com.marlonleoner.musicando.dto.response;

/**
 * Retorno da criação de uma sala. É o único momento em que o segredo da sala é enviado, apenas para quem a criou
 * (host).
 */
public record RoomCreatedResponse(String id, String code, String secret) {
}
