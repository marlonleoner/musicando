package com.marlonleoner.musicando.domain.music;

import java.util.List;
import java.util.Objects;

/**
 * Rodada de uma partida.
 *
 * @param trackId identificador da música no provedor (a prévia é buscada na hora de tocar, pois os links expiram)
 * @param answer  título correto, normalizado
 * @param options 4 títulos exibidos aos jogadores, incluindo a resposta, em ordem aleatória
 * @param artist  artista da música, revelado junto com a resposta
 * @param cover   capa do álbum, revelada junto com a resposta
 */
public record GameRound(String trackId, String answer, List<String> options, String artist, String cover) {
    public GameRound {
        options = Objects.isNull(options) ? List.of() : List.copyOf(options);
    }

    public GameRound(String trackId, String answer, List<String> options) {
        this(trackId, answer, options, null, null);
    }
}
