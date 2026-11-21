package com.marlonleoner.musicando.service.music;

import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.PlaylistNotFoundException;
import com.marlonleoner.musicando.domain.music.MusicPlaylist;
import com.marlonleoner.musicando.domain.music.MusicTrack;
import com.marlonleoner.musicando.integration.MusicProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    @Test
    void shouldReturnProviderPreviewWhenFindingPreview() throws BaseException {
        when(provider.findPreview("10")).thenReturn(Optional.of("preview.mp3"));

        assertThat(service.findPreview("10")).contains("preview.mp3");
    }

    @Test
    void shouldSearchTrimmedQueryWhenSearchingPlaylists() throws BaseException {
        when(provider.searchPlaylists("rock")).thenReturn(List.of(PLAYLIST));

        assertThat(service.searchPlaylists("  rock ")).containsExactly(PLAYLIST);
    }

    @Test
    void shouldCutQueryWhenSearchIsTooLong() throws BaseException {
        when(provider.searchPlaylists("a".repeat(100))).thenReturn(List.of(PLAYLIST));

        assertThat(service.searchPlaylists("a".repeat(150))).containsExactly(PLAYLIST);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldReturnNothingWithoutCallingProviderWhenQueryIsBlank(final String query) throws BaseException {
        assertThat(service.searchPlaylists(query)).isEmpty();
        verify(provider, never()).searchPlaylists(any());
    }

    @Test
    void shouldReturnPlaylistWhenLinkPointsToPlaylist() throws BaseException {
        when(provider.findPlaylistByLink("https://deezer.com/playlist/1")).thenReturn(Optional.of(PLAYLIST));

        assertThat(service.getPlaylistByLinkOrException("https://deezer.com/playlist/1")).isEqualTo(PLAYLIST);
    }

    @Test
    void shouldThrowPlaylistNotFoundExceptionWhenLinkPointsToNothing() throws BaseException {
        when(provider.findPlaylistByLink("https://deezer.com/album/1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPlaylistByLinkOrException("https://deezer.com/album/1")).isInstanceOf(PlaylistNotFoundException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldThrowPlaylistNotFoundExceptionWithoutCallingProviderWhenLinkIsBlank(final String link) throws BaseException {
        assertThatThrownBy(() -> service.getPlaylistByLinkOrException(link)).isInstanceOf(PlaylistNotFoundException.class);
        verify(provider, never()).findPlaylistByLink(any());
    }

    @Test
    void shouldThrowPlaylistNotFoundExceptionWithoutCallingProviderWhenLinkIsTooLong() throws BaseException {
        final String link = "https://deezer.com/playlist/" + "1".repeat(500);

        assertThatThrownBy(() -> service.getPlaylistByLinkOrException(link)).isInstanceOf(PlaylistNotFoundException.class);
        verify(provider, never()).findPlaylistByLink(any());
    }
}
