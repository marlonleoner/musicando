package com.marlonleoner.musicando.domain.music;

import java.util.List;
import java.util.Objects;

/**
 * Músicas sorteadas para tocar de fundo no lobby.
 *
 * @param playlistId playlist de onde as músicas foram sorteadas
 * @param trackIds   identificadores das músicas no provedor (as prévias são buscadas na hora de enviar, pois os links
 *                   expiram)
 */
public record LobbyMusic(String playlistId, List<String> trackIds) {
    public LobbyMusic {
        trackIds = Objects.isNull(trackIds) ? List.of() : List.copyOf(trackIds);
    }
}
