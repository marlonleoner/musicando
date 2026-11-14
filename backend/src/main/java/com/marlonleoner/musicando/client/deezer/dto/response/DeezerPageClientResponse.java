package com.marlonleoner.musicando.client.deezer.dto.response;

import java.util.List;
import java.util.Objects;

/**
 * Página de resultados da Deezer.
 *
 * @param data  itens da página
 * @param total total de itens
 * @param next  URL da próxima página, ausente na última
 * @param error erro, quando a requisição falha
 */
public record DeezerPageClientResponse<T>(List<T> data, Integer total, String next, DeezerErrorClientResponse error) {
    public DeezerPageClientResponse {
        data = Objects.isNull(data) ? List.of() : List.copyOf(data);
    }
}
