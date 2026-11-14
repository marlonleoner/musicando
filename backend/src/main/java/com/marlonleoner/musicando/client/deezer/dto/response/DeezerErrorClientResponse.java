package com.marlonleoner.musicando.client.deezer.dto.response;

/**
 * Erro devolvido pela Deezer. A API responde com HTTP 200 e este objeto no campo {@code error}.
 */
public record DeezerErrorClientResponse(String type, String message, Integer code) {
}
