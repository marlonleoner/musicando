package com.marlonleoner.musicando.integration;

import com.marlonleoner.musicando.client.deezer.DeezerClient;
import com.marlonleoner.musicando.client.deezer.DeezerLinkClient;
import com.marlonleoner.musicando.client.deezer.dto.response.DeezerAlbumClientResponse;
import com.marlonleoner.musicando.client.deezer.dto.response.DeezerPageClientResponse;
import com.marlonleoner.musicando.client.deezer.dto.response.DeezerPlaylistClientResponse;
import com.marlonleoner.musicando.client.deezer.dto.response.DeezerTrackClientResponse;
import com.marlonleoner.musicando.config.properties.DeezerProperties;
import com.marlonleoner.musicando.domain.exception.MusicProviderException;
import com.marlonleoner.musicando.domain.music.MusicPlaylist;
import com.marlonleoner.musicando.domain.music.MusicTrack;
import com.marlonleoner.musicando.util.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link MusicProvider} baseado na API pública da Deezer.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeezerIntegrator implements MusicProvider {

    private static final int TRACKS_PAGE_SIZE = 100;

    private static final int PLAYLISTS_PAGE_SIZE = 100;

    /** Limite de páginas lidas das playlists do perfil, contra uma paginação que nunca acaba. */
    private static final int MAX_PLAYLIST_PAGES = 10;

    /** Saltos seguidos ao abrir um link curto (link.deezer.com → deezer.page.link → deezer.com, por exemplo). */
    private static final int MAX_REDIRECTS = 3;

    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);

    /** Caminho de uma playlist, com ou sem o idioma na frente: /playlist/123, /br/playlist/123, /pt-br/playlist/123. */
    private static final Pattern PLAYLIST_PATH_PATTERN = Pattern.compile("^/(?:[a-z]{2}(?:-[a-z]{2})?/)?playlist/(\\d+)/?$",
            Pattern.CASE_INSENSITIVE);

    private static final Set<String> PLAYLIST_HOSTS = Set.of("deezer.com", "www.deezer.com");

    /** Únicos hosts que o backend abre para seguir um redirecionamento, para não virar um proxy para qualquer URL. */
    private static final Set<String> SHORT_LINK_HOSTS = Set.of("link.deezer.com", "deezer.page.link", "dzr.page.link");

    private static final Set<String> WEB_SCHEMES = Set.of("http", "https");

    private static final String UNAVAILABLE_MESSAGE = "Deezer is unavailable at the moment";

    private final DeezerClient client;

    private final DeezerLinkClient linkClient;

    private final DeezerProperties properties;

    /**
     * Playlists públicas criadas pelo perfil configurado, na ordem da Deezer. Ficam de fora as que ele só favoritou, a de músicas
     * favoritas e as vazias.
     */
    @Override
    public List<MusicPlaylist> featuredPlaylists() throws MusicProviderException {
        final List<MusicPlaylist> playlists = new ArrayList<>();
        boolean hasNext = true;
        int pages = 0;

        while (hasNext && pages < MAX_PLAYLIST_PAGES) {
            final int offset = pages * PLAYLISTS_PAGE_SIZE;
            final DeezerPageClientResponse<DeezerPlaylistClientResponse> page =
                    call(() -> client.userPlaylists(properties.userId(), offset, PLAYLISTS_PAGE_SIZE));

            if (Objects.isNull(page) || Objects.nonNull(page.error())) {
                throw new MusicProviderException(UNAVAILABLE_MESSAGE);
            }

            page.data().stream().filter(this::isOwnPlaylist).map(this::toPlaylist).forEach(playlists::add);
            hasNext = Objects.nonNull(page.next()) && !page.data().isEmpty();
            pages++;
        }

        return List.copyOf(playlists);
    }

    @Override
    public List<MusicPlaylist> searchPlaylists(String query) throws MusicProviderException {
        final DeezerPageClientResponse<DeezerPlaylistClientResponse> page = call(() -> client.searchPlaylists(query, properties.searchLimit()));

        if (Objects.isNull(page) || Objects.nonNull(page.error())) {
            throw new MusicProviderException(UNAVAILABLE_MESSAGE);
        }

        return page.data().stream().filter(this::hasTracks).map(this::toPlaylist).toList();
    }

    @Override
    public Optional<MusicPlaylist> findPlaylistByLink(String link) throws MusicProviderException {
        final Optional<String> playlistId = resolvePlaylistId(link);

        if (playlistId.isEmpty()) {
            return Optional.empty();
        }

        return findPlaylist(playlistId.get());
    }

    @Override
    public Optional<MusicPlaylist> findPlaylist(String id) throws MusicProviderException {
        final DeezerPlaylistClientResponse playlist = call(() -> client.playlist(id));

        if (Objects.isNull(playlist) || Objects.nonNull(playlist.error()) || Objects.isNull(playlist.id())) {
            return Optional.empty();
        }

        return Optional.of(toPlaylist(playlist));
    }

    @Override
    public List<MusicTrack> playlistTracks(String playlistId) throws MusicProviderException {
        final List<MusicTrack> tracks = new ArrayList<>();
        boolean hasNext = true;

        while (hasNext && tracks.size() < properties.maxTracks()) {
            final int offset = tracks.size();
            final DeezerPageClientResponse<DeezerTrackClientResponse> page = call(() -> client.playlistTracks(playlistId, offset, TRACKS_PAGE_SIZE));

            if (Objects.isNull(page) || Objects.nonNull(page.error())) {
                throw new MusicProviderException(UNAVAILABLE_MESSAGE);
            }

            page.data().stream().map(this::toTrack).forEach(tracks::add);
            hasNext = Objects.nonNull(page.next()) && !page.data().isEmpty();
        }

        return List.copyOf(tracks.subList(0, Math.min(tracks.size(), properties.maxTracks())));
    }

    @Override
    public Optional<String> findPreview(String trackId) throws MusicProviderException {
        final DeezerTrackClientResponse track = call(() -> client.track(trackId));

        if (Objects.isNull(track) || Objects.nonNull(track.error()) || !hasPreview(track)) {
            return Optional.empty();
        }

        return Optional.of(track.preview());
    }

    private boolean isOwnPlaylist(DeezerPlaylistClientResponse playlist) {
        final boolean ownedByUser = Objects.nonNull(playlist.creator()) && Objects.nonNull(playlist.creator().id())
                && properties.userId().equals(String.valueOf(playlist.creator().id()));

        return ownedByUser && !Boolean.TRUE.equals(playlist.lovedTrack()) && hasTracks(playlist);
    }

    private boolean hasTracks(DeezerPlaylistClientResponse playlist) {
        return Objects.nonNull(playlist.nbTracks()) && playlist.nbTracks() > 0;
    }

    /**
     * Identificador da playlist apontada pelo link. Links curtos são abertos, sem seguir o redirecionamento, até chegar a um
     * endereço de playlist do site da Deezer; qualquer host fora da lista encerra a busca.
     */
    private Optional<String> resolvePlaylistId(String text) throws MusicProviderException {
        Optional<URI> current = extractUri(text);
        int redirects = 0;

        while (current.isPresent()) {
            final URI uri = current.get();
            final String host = hostOf(uri);

            if (PLAYLIST_HOSTS.contains(host)) {
                return playlistIdOf(uri);
            }

            if (!SHORT_LINK_HOSTS.contains(host) || redirects >= MAX_REDIRECTS) {
                return Optional.empty();
            }

            current = redirectOf(uri);
            redirects++;
        }

        return Optional.empty();
    }

    private Optional<URI> extractUri(String text) {
        if (Objects.isNull(text) || text.isBlank()) {
            return Optional.empty();
        }

        final String trimmed = text.strip();
        final Matcher matcher = URL_PATTERN.matcher(trimmed);

        return parseUri(matcher.find() ? matcher.group() : "https://" + trimmed);
    }

    private Optional<URI> parseUri(String value) {
        try {
            return Optional.of(new URI(value));
        } catch (URISyntaxException ex) {
            return Optional.empty();
        }
    }

    /**
     * Host em minúsculas, ou vazio quando o endereço não é http(s).
     */
    private String hostOf(URI uri) {
        final String scheme = Objects.isNull(uri.getScheme()) ? "" : uri.getScheme().toLowerCase(Locale.ROOT);

        if (!WEB_SCHEMES.contains(scheme) || Objects.isNull(uri.getHost())) {
            return "";
        }

        return uri.getHost().toLowerCase(Locale.ROOT);
    }

    private Optional<String> playlistIdOf(URI uri) {
        final Matcher matcher = PLAYLIST_PATH_PATTERN.matcher(Objects.requireNonNullElse(uri.getPath(), ""));

        return matcher.matches() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    private Optional<URI> redirectOf(URI uri) throws MusicProviderException {
        final ResponseEntity<Void> response;

        try {
            response = linkClient.open(uri);
        } catch (HttpClientErrorException ex) {
            return Optional.empty();
        } catch (RestClientException ex) {
            log.warn("Deezer link request failed: {}", LogSanitizer.sanitize(ex.getMessage()));
            throw new MusicProviderException(UNAVAILABLE_MESSAGE);
        }

        if (Objects.isNull(response) || !response.getStatusCode().is3xxRedirection() || Objects.isNull(response.getHeaders().getLocation())) {
            return Optional.empty();
        }

        return Optional.of(uri.resolve(response.getHeaders().getLocation()));
    }

    private boolean hasPreview(DeezerTrackClientResponse track) {
        return Objects.nonNull(track.preview()) && !track.preview().isBlank();
    }

    private <T> T call(Supplier<T> request) throws MusicProviderException {
        try {
            return request.get();
        } catch (RestClientException ex) {
            log.warn("Deezer request failed: {}", LogSanitizer.sanitize(ex.getMessage()));
            throw new MusicProviderException(UNAVAILABLE_MESSAGE);
        }
    }

    private MusicPlaylist toPlaylist(DeezerPlaylistClientResponse playlist) {
        return new MusicPlaylist(String.valueOf(playlist.id()), playlist.title(), playlist.pictureMedium(),
                Objects.requireNonNullElse(playlist.nbTracks(), 0));
    }

    private String coverOf(DeezerAlbumClientResponse album) {
        return Objects.nonNull(album.coverBig()) ? album.coverBig() : album.coverMedium();
    }

    private MusicTrack toTrack(DeezerTrackClientResponse track) {
        final String title = Objects.nonNull(track.titleShort()) && !track.titleShort().isBlank() ? track.titleShort() : track.title();
        final String artist = Objects.isNull(track.artist()) ? null : track.artist().name();
        final String cover = Objects.isNull(track.album()) ? null : coverOf(track.album());
        final boolean playable = !Boolean.FALSE.equals(track.readable()) && hasPreview(track);

        return new MusicTrack(String.valueOf(track.id()), title, artist, cover, playable);
    }
}
