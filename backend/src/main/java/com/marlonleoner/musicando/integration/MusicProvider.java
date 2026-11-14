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
}
