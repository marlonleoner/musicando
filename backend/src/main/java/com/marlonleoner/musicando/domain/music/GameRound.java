package com.marlonleoner.musicando.domain.music;

import java.util.List;
import java.util.Objects;

/**
 * Rodada de uma partida.
 *
 * @param trackId       identificador da música no provedor (a prévia é buscada na hora de tocar, pois os links expiram)
 * @param answer        título correto, normalizado
 * @param options       4 títulos exibidos aos jogadores, incluindo a resposta, em ordem aleatória
 * @param artist        artista da música, revelado junto com a resposta
 * @param cover         capa do álbum, revelada junto com a resposta
 * @param startFraction ponto de início da prévia, de 0 (começo) a 1 (último ponto que ainda deixa tocar a rodada e a
 *                      revelação inteiras); a TV converte em segundos com a duração real da prévia
 */
public record GameRound(String trackId, String answer, List<String> options, String artist, String cover, Double startFraction) {
    public GameRound {
        options = Objects.isNull(options) ? List.of() : List.copyOf(options);
        // Partidas gravadas no Redis antes desse campo existir não têm o valor: tocam do começo.
        startFraction = Objects.isNull(startFraction) ? 0.0 : Math.clamp(startFraction, 0.0, 1.0);
    }

    public GameRound(String trackId, String answer, List<String> options, String artist, String cover) {
        this(trackId, answer, options, artist, cover, 0.0);
    }

    public GameRound(String trackId, String answer, List<String> options) {
        this(trackId, answer, options, null, null);
    }
}
