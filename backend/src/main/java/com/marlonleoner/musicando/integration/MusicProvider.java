package com.marlonleoner.musicando.integration;

import com.marlonleoner.musicando.domain.exception.MusicProviderException;
import com.marlonleoner.musicando.domain.music.MusicPlaylist;
import com.marlonleoner.musicando.domain.music.MusicTrack;

import java.util.List;
import java.util.Optional;

/**
 * Provedor de músicas (Deezer hoje; outros, como iTunes, no futuro).
 */
public interface MusicProvider {

    /**
     * Playlists oferecidas por padrão ao VIP.
     */
    List<MusicPlaylist> featuredPlaylists() throws MusicProviderException;

    /**
     * Playlists encontradas pelo nome.
     */
    List<MusicPlaylist> searchPlaylists(String query) throws MusicProviderException;

    /**
     * Playlist apontada por um link do provedor. Aceita o texto de compartilhamento inteiro, desde que contenha o link.
     */
    Optional<MusicPlaylist> findPlaylistByLink(String link) throws MusicProviderException;

    Optional<MusicPlaylist> findPlaylist(String id) throws MusicProviderException;

    List<MusicTrack> playlistTracks(String playlistId) throws MusicProviderException;

    /**
     * Prévia atual da música. Os links das prévias expiram, por isso são buscados na hora de tocar.
     */
    Optional<String> findPreview(String trackId) throws MusicProviderException;
}
