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

    List<MusicPlaylist> featuredPlaylists() throws MusicProviderException;

    Optional<MusicPlaylist> findPlaylist(String id) throws MusicProviderException;

    List<MusicTrack> playlistTracks(String playlistId) throws MusicProviderException;

    /**
     * Prévia atual da música. Os links das prévias expiram, por isso são buscados na hora de tocar.
     */
    Optional<String> findPreview(String trackId) throws MusicProviderException;
}
