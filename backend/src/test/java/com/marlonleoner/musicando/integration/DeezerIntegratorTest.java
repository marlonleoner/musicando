package com.marlonleoner.musicando.integration;

import com.marlonleoner.musicando.client.deezer.DeezerClient;
import com.marlonleoner.musicando.client.deezer.dto.response.DeezerArtistClientResponse;
import com.marlonleoner.musicando.client.deezer.dto.response.DeezerErrorClientResponse;
import com.marlonleoner.musicando.client.deezer.dto.response.DeezerPageClientResponse;
import com.marlonleoner.musicando.client.deezer.dto.response.DeezerPlaylistClientResponse;
import com.marlonleoner.musicando.client.deezer.dto.response.DeezerTrackClientResponse;
import com.marlonleoner.musicando.config.properties.DeezerProperties;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.MusicProviderException;
import com.marlonleoner.musicando.domain.music.MusicPlaylist;
import com.marlonleoner.musicando.domain.music.MusicTrack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeezerIntegratorTest {

    private static final String PLAYLIST_ID = "908622995";

    private static final String NEXT = "https://api.deezer.com/next";

    private static final DeezerErrorClientResponse ERROR = new DeezerErrorClientResponse("DataException", "no data", 800);

    @Mock
    private DeezerClient client;

    private DeezerIntegrator integrator;

    @BeforeEach
    void setUp() {
        integrator = new DeezerIntegrator(client, new DeezerProperties("https://api.deezer.com", Duration.ofSeconds(5), 25, 150));
    }

    private static DeezerPlaylistClientResponse playlist(final Long id, final Integer tracks) {
        return new DeezerPlaylistClientResponse(id, "Top Brasil", tracks, "cover.jpg", null);
    }

    private static DeezerTrackClientResponse track(final long id) {
        return new DeezerTrackClientResponse(id, "Song " + id, null, true, "preview.mp3", new DeezerArtistClientResponse("Artist"));
    }

    private static DeezerPageClientResponse<DeezerTrackClientResponse> tracksPage(final int from, final int to, final String next) {
        return new DeezerPageClientResponse<>(IntStream.range(from, to).mapToObj(DeezerIntegratorTest::track).toList(), 500, next, null);
    }

    @Test
    void shouldMapPlaylistsWhenListingFeaturedPlaylists() throws BaseException {
        when(client.chartPlaylists(25)).thenReturn(new DeezerPageClientResponse<>(List.of(playlist(1L, 80), playlist(2L, null)), 2, null, null));

        assertThat(integrator.featuredPlaylists()).containsExactly(
                new MusicPlaylist("1", "Top Brasil", "cover.jpg", 80),
                new MusicPlaylist("2", "Top Brasil", "cover.jpg", 0));
    }

    @Test
    void shouldThrowMusicProviderExceptionWhenFeaturedPlaylistsReturnError() {
        when(client.chartPlaylists(25)).thenReturn(new DeezerPageClientResponse<>(null, null, null, ERROR));

        assertThatThrownBy(() -> integrator.featuredPlaylists()).isInstanceOf(MusicProviderException.class);
    }

    @Test
    void shouldThrowMusicProviderExceptionWhenFeaturedPlaylistsReturnNothing() {
        when(client.chartPlaylists(25)).thenReturn(null);

        assertThatThrownBy(() -> integrator.featuredPlaylists()).isInstanceOf(MusicProviderException.class);
    }

    @Test
    void shouldThrowMusicProviderExceptionWhenDeezerIsUnreachable() {
        when(client.chartPlaylists(25)).thenThrow(new ResourceAccessException("timeout"));

        assertThatThrownBy(() -> integrator.featuredPlaylists()).isInstanceOf(MusicProviderException.class);
    }

    @Test
    void shouldReturnPlaylistWhenPlaylistExists() throws BaseException {
        when(client.playlist(PLAYLIST_ID)).thenReturn(playlist(908622995L, 50));

        assertThat(integrator.findPlaylist(PLAYLIST_ID)).contains(new MusicPlaylist(PLAYLIST_ID, "Top Brasil", "cover.jpg", 50));
    }

    @Test
    void shouldReturnEmptyWhenPlaylistReturnsError() throws BaseException {
        when(client.playlist(PLAYLIST_ID)).thenReturn(new DeezerPlaylistClientResponse(null, null, null, null, ERROR));

        assertThat(integrator.findPlaylist(PLAYLIST_ID)).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenPlaylistHasNoId() throws BaseException {
        when(client.playlist(PLAYLIST_ID)).thenReturn(playlist(null, 50));

        assertThat(integrator.findPlaylist(PLAYLIST_ID)).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenPlaylistReturnsNothing() throws BaseException {
        when(client.playlist(PLAYLIST_ID)).thenReturn(null);

        assertThat(integrator.findPlaylist(PLAYLIST_ID)).isEmpty();
    }

    @Test
    void shouldReadAllPagesWhenPlaylistHasNextPage() throws BaseException {
        when(client.playlistTracks(PLAYLIST_ID, 0, 100)).thenReturn(tracksPage(0, 100, NEXT));
        when(client.playlistTracks(PLAYLIST_ID, 100, 100)).thenReturn(tracksPage(100, 120, null));

        final List<MusicTrack> tracks = integrator.playlistTracks(PLAYLIST_ID);

        assertThat(tracks).hasSize(120);
        assertThat(tracks.getFirst()).isEqualTo(new MusicTrack("0", "Song 0", "Artist", true));
    }

    @Test
    void shouldStopAtMaxTracksWhenPlaylistIsLarger() throws BaseException {
        when(client.playlistTracks(PLAYLIST_ID, 0, 100)).thenReturn(tracksPage(0, 100, NEXT));
        when(client.playlistTracks(PLAYLIST_ID, 100, 100)).thenReturn(tracksPage(100, 200, NEXT));

        assertThat(integrator.playlistTracks(PLAYLIST_ID)).hasSize(150);
        verify(client, never()).playlistTracks(eq(PLAYLIST_ID), eq(200), anyInt());
    }

    @Test
    void shouldStopWhenPageIsEmpty() throws BaseException {
        when(client.playlistTracks(PLAYLIST_ID, 0, 100)).thenReturn(new DeezerPageClientResponse<>(List.of(), 0, NEXT, null));

        assertThat(integrator.playlistTracks(PLAYLIST_ID)).isEmpty();
    }

    @Test
    void shouldThrowMusicProviderExceptionWhenTracksPageReturnsError() {
        when(client.playlistTracks(PLAYLIST_ID, 0, 100)).thenReturn(new DeezerPageClientResponse<>(null, null, null, ERROR));

        assertThatThrownBy(() -> integrator.playlistTracks(PLAYLIST_ID)).isInstanceOf(MusicProviderException.class);
    }

    @Test
    void shouldThrowMusicProviderExceptionWhenTracksPageReturnsNothing() {
        when(client.playlistTracks(PLAYLIST_ID, 0, 100)).thenReturn(null);

        assertThatThrownBy(() -> integrator.playlistTracks(PLAYLIST_ID)).isInstanceOf(MusicProviderException.class);
    }

    @ParameterizedTest
    @CsvSource(nullValues = "NULL", value = {
        "Song (Live), Song, true, preview.mp3, Song, true",
        "Song (Live), '  ', true, preview.mp3, Song (Live), true",
        "Song (Live), NULL, NULL, preview.mp3, Song (Live), true",
        "Song, Song, false, preview.mp3, Song, false",
        "Song, Song, true, '', Song, false",
        "Song, Song, true, NULL, Song, false",
    })
    void shouldMapTitleAndPlayableWhenReadingTracks(final String title, final String titleShort, final Boolean readable, final String preview,
                                                    final String expectedTitle, final boolean expectedPlayable) throws BaseException {
        final DeezerTrackClientResponse track = new DeezerTrackClientResponse(1L, title, titleShort, readable, preview, null);
        when(client.playlistTracks(PLAYLIST_ID, 0, 100)).thenReturn(new DeezerPageClientResponse<>(List.of(track), 1, null, null));

        assertThat(integrator.playlistTracks(PLAYLIST_ID)).containsExactly(new MusicTrack("1", expectedTitle, null, expectedPlayable));
    }
}
