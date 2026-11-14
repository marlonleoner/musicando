package com.marlonleoner.musicando.service.music;

import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.PlaylistNotFoundException;
import com.marlonleoner.musicando.domain.music.MusicPlaylist;
import com.marlonleoner.musicando.domain.music.MusicTrack;
import com.marlonleoner.musicando.integration.MusicProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MusicServiceTest {

    private static final MusicPlaylist PLAYLIST = new MusicPlaylist("1", "Hits", "cover.jpg", 10);

    @Mock
    private MusicProvider provider;

    @InjectMocks
    private MusicService service;

    @Test
    void shouldReturnProviderPlaylistsWhenListingFeaturedPlaylists() throws BaseException {
        when(provider.featuredPlaylists()).thenReturn(List.of(PLAYLIST));

        assertThat(service.featuredPlaylists()).containsExactly(PLAYLIST);
    }

    @Test
    void shouldReturnPlaylistWhenPlaylistExists() throws BaseException {
        when(provider.findPlaylist("1")).thenReturn(Optional.of(PLAYLIST));

        assertThat(service.getPlaylistOrException("1")).isEqualTo(PLAYLIST);
    }

    @Test
    void shouldThrowPlaylistNotFoundExceptionWhenPlaylistDoesNotExist() throws BaseException {
        when(provider.findPlaylist("1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPlaylistOrException("1")).isInstanceOf(PlaylistNotFoundException.class);
    }

    @Test
    void shouldReturnProviderTracksWhenListingPlaylistTracks() throws BaseException {
        final List<MusicTrack> tracks = List.of(new MusicTrack("10", "Song", "Artist", true));
        when(provider.playlistTracks("1")).thenReturn(tracks);

        assertThat(service.playlistTracks("1")).isEqualTo(tracks);
    }
}
