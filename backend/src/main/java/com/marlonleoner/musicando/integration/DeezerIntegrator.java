package com.marlonleoner.musicando.integration;

import com.marlonleoner.musicando.client.deezer.DeezerClient;
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
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * {@link MusicProvider} baseado na API pública da Deezer.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeezerIntegrator implements MusicProvider {

    private static final int TRACKS_PAGE_SIZE = 100;

    private static final String UNAVAILABLE_MESSAGE = "Deezer is unavailable at the moment";

    private final DeezerClient client;

    private final DeezerProperties properties;

    @Override
    public List<MusicPlaylist> featuredPlaylists() throws MusicProviderException {
        final DeezerPageClientResponse<DeezerPlaylistClientResponse> page = call(() -> client.chartPlaylists(properties.featuredLimit()));

        if (Objects.isNull(page) || Objects.nonNull(page.error())) {
            throw new MusicProviderException(UNAVAILABLE_MESSAGE);
        }

        return page.data().stream().map(this::toPlaylist).toList();
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

    private MusicTrack toTrack(DeezerTrackClientResponse track) {
        final String title = Objects.nonNull(track.titleShort()) && !track.titleShort().isBlank() ? track.titleShort() : track.title();
        final String artist = Objects.isNull(track.artist()) ? null : track.artist().name();
        final boolean playable = !Boolean.FALSE.equals(track.readable()) && Objects.nonNull(track.preview()) && !track.preview().isBlank();

        return new MusicTrack(String.valueOf(track.id()), title, artist, playable);
    }
}
