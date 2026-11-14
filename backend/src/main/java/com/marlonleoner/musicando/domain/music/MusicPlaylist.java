package com.marlonleoner.musicando.domain.music;

/**
 * Playlist de um provedor de músicas.
 *
 * @param id         identificador no provedor
 * @param name       nome da playlist
 * @param cover      URL da capa
 * @param trackCount quantidade de músicas
 */
public record MusicPlaylist(String id, String name, String cover, int trackCount) {
}
