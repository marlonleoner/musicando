package com.marlonleoner.musicando.service.music;

import com.marlonleoner.musicando.domain.exception.MusicProviderException;
import com.marlonleoner.musicando.domain.exception.PlaylistNotFoundException;
import com.marlonleoner.musicando.domain.music.MusicPlaylist;
import com.marlonleoner.musicando.domain.music.MusicTrack;
import com.marlonleoner.musicando.integration.MusicProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MusicService {

    private final MusicProvider provider;

    public List<MusicPlaylist> featuredPlaylists() throws MusicProviderException {
        return provider.featuredPlaylists();
    }

    public MusicPlaylist getPlaylistOrException(String playlistId) throws MusicProviderException, PlaylistNotFoundException {
        return provider.findPlaylist(playlistId).orElseThrow(() -> new PlaylistNotFoundException(playlistId));
    }

    public List<MusicTrack> playlistTracks(String playlistId) throws MusicProviderException {
        return provider.playlistTracks(playlistId);
    }
}
