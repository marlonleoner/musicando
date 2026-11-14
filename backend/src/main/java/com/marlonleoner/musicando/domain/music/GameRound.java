package com.marlonleoner.musicando.domain.music;

import java.util.List;
import java.util.Objects;

/**
 * Rodada de uma partida.
 *
 * @param trackId identificador da música no provedor (a prévia é buscada na hora de tocar, pois os links expiram)
 * @param answer  título correto, normalizado
 * @param options 4 títulos exibidos aos jogadores, incluindo a resposta, em ordem aleatória
 */
public record GameRound(String trackId, String answer, List<String> options) {
    public GameRound {
        options = Objects.isNull(options) ? List.of() : List.copyOf(options);
    }
}
