package com.marlonleoner.musicando.integration;

import com.marlonleoner.musicando.client.deezer.DeezerClient;
import com.marlonleoner.musicando.client.deezer.DeezerLinkClient;
import com.marlonleoner.musicando.client.deezer.dto.response.DeezerAlbumClientResponse;
import com.marlonleoner.musicando.client.deezer.dto.response.DeezerArtistClientResponse;
import com.marlonleoner.musicando.client.deezer.dto.response.DeezerErrorClientResponse;
import com.marlonleoner.musicando.client.deezer.dto.response.DeezerPageClientResponse;
import com.marlonleoner.musicando.client.deezer.dto.response.DeezerPlaylistClientResponse;
import com.marlonleoner.musicando.client.deezer.dto.response.DeezerTrackClientResponse;
import com.marlonleoner.musicando.client.deezer.dto.response.DeezerUserClientResponse;
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
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeezerIntegratorTest {

    private static final String PLAYLIST_ID = "908622995";

    private static final String NEXT = "https://api.deezer.com/next";

    private static final String USER_ID = "2037081106";

    private static final MusicPlaylist FOUND = new MusicPlaylist(PLAYLIST_ID, "Top Brasil", "cover.jpg", 50);

    private static final DeezerErrorClientResponse ERROR = new DeezerErrorClientResponse("DataException", "no data", 800);

    @Mock
    private DeezerClient client;

    @Mock
    private DeezerLinkClient linkClient;

    private DeezerIntegrator integrator;

    @BeforeEach
    void setUp() {
        integrator = new DeezerIntegrator(client, linkClient, new DeezerProperties("https://api.deezer.com", Duration.ofSeconds(5), USER_ID, 20, 150));
    }

    private static DeezerPlaylistClientResponse playlist(final Long id, final Integer tracks) {
        return new DeezerPlaylistClientResponse(id, "Top Brasil", tracks, "cover.jpg", null, null, null);
    }

    private static DeezerPlaylistClientResponse userPlaylist(final Long id, final Integer tracks, final Long creatorId, final Boolean lovedTrack) {
        return new DeezerPlaylistClientResponse(id, "Top Brasil", tracks, "cover.jpg", lovedTrack, new DeezerUserClientResponse(creatorId), null);
    }

    private static DeezerPageClientResponse<DeezerPlaylistClientResponse> playlistsPage(final String next, final DeezerPlaylistClientResponse... playlists) {
        return new DeezerPageClientResponse<>(List.of(playlists), playlists.length, next, null);
    }

    private static ResponseEntity<Void> redirectTo(final String location) {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(location)).build();
    }

    private static DeezerTrackClientResponse track(final long id) {
        return new DeezerTrackClientResponse(id, "Song " + id, null, true, "preview.mp3", new DeezerArtistClientResponse("Artist"), null, null);
    }

    private static DeezerPageClientResponse<DeezerTrackClientResponse> tracksPage(final int from, final int to, final String next) {
        return new DeezerPageClientResponse<>(IntStream.range(from, to).mapToObj(DeezerIntegratorTest::track).toList(), 500, next, null);
    }

    @Test
    void shouldKeepOnlyOwnPlaylistsWithTracksWhenListingFeaturedPlaylists() throws BaseException {
        when(client.userPlaylists(USER_ID, 0, 100)).thenReturn(playlistsPage(null,
                userPlaylist(1L, 80, 2037081106L, false),
                userPlaylist(2L, 30, 99L, false),
                userPlaylist(3L, 120, 2037081106L, true),
                userPlaylist(4L, 0, 2037081106L, false),
                userPlaylist(5L, null, 2037081106L, null),
                userPlaylist(6L, 15, null, null),
                playlist(7L, 15),
                userPlaylist(8L, 12, 2037081106L, null)));

        assertThat(integrator.featuredPlaylists()).containsExactly(
                new MusicPlaylist("1", "Top Brasil", "cover.jpg", 80),
                new MusicPlaylist("8", "Top Brasil", "cover.jpg", 12));
    }

    @Test
    void shouldReadAllPagesWhenUserHasMorePlaylists() throws BaseException {
        when(client.userPlaylists(USER_ID, 0, 100)).thenReturn(playlistsPage(NEXT, userPlaylist(1L, 10, 2037081106L, false)));
        when(client.userPlaylists(USER_ID, 100, 100)).thenReturn(playlistsPage(null, userPlaylist(2L, 20, 2037081106L, false)));

        assertThat(integrator.featuredPlaylists()).extracting(MusicPlaylist::id).containsExactly("1", "2");
    }

    @Test
    void shouldStopAtMaxPagesWhenUserPlaylistsNeverEnd() throws BaseException {
        when(client.userPlaylists(eq(USER_ID), anyInt(), eq(100))).thenReturn(playlistsPage(NEXT, userPlaylist(1L, 10, 2037081106L, false)));

        assertThat(integrator.featuredPlaylists()).hasSize(10);
        verify(client, times(10)).userPlaylists(eq(USER_ID), anyInt(), eq(100));
    }

    @Test
    void shouldStopWhenUserPlaylistsPageIsEmpty() throws BaseException {
        when(client.userPlaylists(USER_ID, 0, 100)).thenReturn(playlistsPage(NEXT));

        assertThat(integrator.featuredPlaylists()).isEmpty();
        verify(client, times(1)).userPlaylists(eq(USER_ID), anyInt(), eq(100));
    }

    @Test
    void shouldThrowMusicProviderExceptionWhenUserPlaylistsReturnError() {
        when(client.userPlaylists(USER_ID, 0, 100)).thenReturn(new DeezerPageClientResponse<>(null, null, null, ERROR));

        assertThatThrownBy(() -> integrator.featuredPlaylists()).isInstanceOf(MusicProviderException.class);
    }

    @Test
    void shouldThrowMusicProviderExceptionWhenUserPlaylistsReturnNothing() {
        when(client.userPlaylists(USER_ID, 0, 100)).thenReturn(null);

        assertThatThrownBy(() -> integrator.featuredPlaylists()).isInstanceOf(MusicProviderException.class);
    }

    @Test
    void shouldThrowMusicProviderExceptionWhenDeezerIsUnreachable() {
        when(client.userPlaylists(USER_ID, 0, 100)).thenThrow(new ResourceAccessException("timeout"));

        assertThatThrownBy(() -> integrator.featuredPlaylists()).isInstanceOf(MusicProviderException.class);
    }

    @Test
    void shouldReturnPlaylistsWithTracksWhenSearchingPlaylists() throws BaseException {
        when(client.searchPlaylists("rock", 20)).thenReturn(playlistsPage(null, playlist(1L, 40), playlist(2L, 0), playlist(3L, null)));

        assertThat(integrator.searchPlaylists("rock")).containsExactly(new MusicPlaylist("1", "Top Brasil", "cover.jpg", 40));
    }

    @Test
    void shouldThrowMusicProviderExceptionWhenSearchReturnsError() {
        when(client.searchPlaylists("rock", 20)).thenReturn(new DeezerPageClientResponse<>(null, null, null, ERROR));

        assertThatThrownBy(() -> integrator.searchPlaylists("rock")).isInstanceOf(MusicProviderException.class);
    }

    @Test
    void shouldThrowMusicProviderExceptionWhenSearchReturnsNothing() {
        when(client.searchPlaylists("rock", 20)).thenReturn(null);

        assertThatThrownBy(() -> integrator.searchPlaylists("rock")).isInstanceOf(MusicProviderException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "https://www.deezer.com/br/playlist/908622995",
        "https://deezer.com/playlist/908622995",
        "deezer.com/en/playlist/908622995?utm_source=share",
        "  www.deezer.com/pt-br/playlist/908622995/  ",
        "HTTP://WWW.DEEZER.COM/playlist/908622995",
        "Ouça Top Brasil no Deezer: https://www.deezer.com/br/playlist/908622995 até já"
    })
    void shouldFindPlaylistWithoutOpeningLinkWhenLinkPointsToPlaylist(final String link) throws BaseException {
        when(client.playlist(PLAYLIST_ID)).thenReturn(playlist(908622995L, 50));

        assertThat(integrator.findPlaylistByLink(link)).contains(FOUND);
        verify(linkClient, never()).open(any());
    }

    @Test
    void shouldFollowRedirectsWhenLinkIsShort() throws BaseException {
        when(linkClient.open(URI.create("https://link.deezer.com/s/abc"))).thenReturn(redirectTo("https://deezer.page.link/xyz"));
        when(linkClient.open(URI.create("https://deezer.page.link/xyz"))).thenReturn(redirectTo("https://www.deezer.com/br/playlist/908622995?deferredFl=1"));
        when(client.playlist(PLAYLIST_ID)).thenReturn(playlist(908622995L, 50));

        assertThat(integrator.findPlaylistByLink("Ouça no Deezer https://link.deezer.com/s/abc")).contains(FOUND);
    }

    @Test
    void shouldResolveRelativeLocationWhenFollowingShortLink() throws BaseException {
        when(linkClient.open(URI.create("https://dzr.page.link/xyz"))).thenReturn(redirectTo("/xyz2"));
        when(linkClient.open(URI.create("https://dzr.page.link/xyz2"))).thenReturn(redirectTo("https://deezer.com/playlist/908622995"));
        when(client.playlist(PLAYLIST_ID)).thenReturn(playlist(908622995L, 50));

        assertThat(integrator.findPlaylistByLink("https://dzr.page.link/xyz")).contains(FOUND);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "   ",
        "rock nacional",
        "https://evil.example.com/playlist/908622995",
        "https://www.deezer.com/br/album/908622995",
        "https://www.deezer.com/br/playlist/abc",
        "ftp://www.deezer.com/playlist/908622995",
        "https://deezer.com:invalid/playlist/1"
    })
    void shouldReturnEmptyWithoutRequestsWhenLinkIsNotAPlaylist(final String link) throws BaseException {
        assertThat(integrator.findPlaylistByLink(link)).isEmpty();
        verify(linkClient, never()).open(any());
        verify(client, never()).playlist(any());
    }

    @Test
    void shouldReturnEmptyWhenShortLinkDoesNotRedirect() throws BaseException {
        when(linkClient.open(any())).thenReturn(ResponseEntity.ok().build());

        assertThat(integrator.findPlaylistByLink("https://link.deezer.com/s/abc")).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenRedirectHasNoLocation() throws BaseException {
        when(linkClient.open(any())).thenReturn(ResponseEntity.status(HttpStatus.FOUND).build());

        assertThat(integrator.findPlaylistByLink("https://link.deezer.com/s/abc")).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenShortLinkReturnsNothing() throws BaseException {
        when(linkClient.open(any())).thenReturn(null);

        assertThat(integrator.findPlaylistByLink("https://link.deezer.com/s/abc")).isEmpty();
    }

    @Test
    void shouldNotFollowRedirectWhenShortLinkPointsOutsideDeezer() throws BaseException {
        when(linkClient.open(any())).thenReturn(redirectTo("https://evil.example.com/next"));

        assertThat(integrator.findPlaylistByLink("https://link.deezer.com/s/abc")).isEmpty();
        verify(linkClient, times(1)).open(any());
    }

    @Test
    void shouldStopAtMaxRedirectsWhenShortLinkLoops() throws BaseException {
        when(linkClient.open(any())).thenReturn(redirectTo("https://link.deezer.com/s/loop"));

        assertThat(integrator.findPlaylistByLink("https://link.deezer.com/s/abc")).isEmpty();
        verify(linkClient, times(3)).open(any());
    }

    @Test
    void shouldReturnEmptyWhenShortLinkIsNotFound() throws BaseException {
        when(linkClient.open(any())).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThat(integrator.findPlaylistByLink("https://link.deezer.com/s/abc")).isEmpty();
    }

    @Test
    void shouldThrowMusicProviderExceptionWhenShortLinkRequestFails() {
        when(linkClient.open(any())).thenThrow(new ResourceAccessException("timeout"));

        assertThatThrownBy(() -> integrator.findPlaylistByLink("https://link.deezer.com/s/abc")).isInstanceOf(MusicProviderException.class);
    }

    @Test
    void shouldReturnPlaylistWhenPlaylistExists() throws BaseException {
        when(client.playlist(PLAYLIST_ID)).thenReturn(playlist(908622995L, 50));

        assertThat(integrator.findPlaylist(PLAYLIST_ID)).contains(new MusicPlaylist(PLAYLIST_ID, "Top Brasil", "cover.jpg", 50));
    }

    @Test
    void shouldReturnEmptyWhenPlaylistReturnsError() throws BaseException {
        when(client.playlist(PLAYLIST_ID)).thenReturn(new DeezerPlaylistClientResponse(null, null, null, null, null, null, ERROR));

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
        final DeezerTrackClientResponse track = new DeezerTrackClientResponse(1L, title, titleShort, readable, preview, null, null, null);
        when(client.playlistTracks(PLAYLIST_ID, 0, 100)).thenReturn(new DeezerPageClientResponse<>(List.of(track), 1, null, null));

        assertThat(integrator.playlistTracks(PLAYLIST_ID)).containsExactly(new MusicTrack("1", expectedTitle, null, expectedPlayable));
    }

    @Test
    void shouldReturnPreviewWhenTrackHasPreview() throws BaseException {
        when(client.track("10")).thenReturn(track(10));

        assertThat(integrator.findPreview("10")).contains("preview.mp3");
    }

    @ParameterizedTest
    @CsvSource(nullValues = "NULL", value = {"NULL, false", "'', false", "preview.mp3, true"})
    void shouldReturnEmptyWhenTrackHasNoPreviewOrReturnsError(final String preview, final boolean withError) throws BaseException {
        when(client.track("10")).thenReturn(new DeezerTrackClientResponse(10L, "Song", null, true, preview, null, null, withError ? ERROR : null));

        assertThat(integrator.findPreview("10")).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenTrackReturnsNothing() throws BaseException {
        when(client.track("10")).thenReturn(null);

        assertThat(integrator.findPreview("10")).isEmpty();
    }

    @Test
    void shouldThrowMusicProviderExceptionWhenTrackRequestFails() {
        when(client.track("10")).thenThrow(new ResourceAccessException("timeout"));

        assertThatThrownBy(() -> integrator.findPreview("10")).isInstanceOf(MusicProviderException.class);
    }

    @ParameterizedTest
    @CsvSource(nullValues = "NULL", value = {"m.jpg, b.jpg, b.jpg", "m.jpg, NULL, m.jpg", "NULL, NULL, NULL"})
    void shouldPreferBigCoverWhenReadingTracks(final String medium, final String big, final String expected) throws BaseException {
        final DeezerTrackClientResponse track = new DeezerTrackClientResponse(1L, "Song", null, true, "p.mp3", null,
                new DeezerAlbumClientResponse(medium, big), null);
        when(client.playlistTracks(PLAYLIST_ID, 0, 100)).thenReturn(new DeezerPageClientResponse<>(List.of(track), 1, null, null));

        assertThat(integrator.playlistTracks(PLAYLIST_ID).getFirst().cover()).isEqualTo(expected);
    }

    @Test
    void shouldHaveNoCoverWhenTrackHasNoAlbum() throws BaseException {
        when(client.playlistTracks(PLAYLIST_ID, 0, 100)).thenReturn(tracksPage(0, 1, null));

        assertThat(integrator.playlistTracks(PLAYLIST_ID).getFirst().cover()).isNull();
    }
}
