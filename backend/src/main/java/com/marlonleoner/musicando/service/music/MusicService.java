package com.marlonleoner.musicando.service.music;

import com.marlonleoner.musicando.domain.exception.MusicProviderException;
import com.marlonleoner.musicando.domain.exception.PlaylistNotFoundException;
import com.marlonleoner.musicando.domain.music.MusicPlaylist;
import com.marlonleoner.musicando.domain.music.MusicTrack;
import com.marlonleoner.musicando.integration.MusicProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MusicService {

    /** Buscas maiores são cortadas: nenhum nome de playlist útil passa disso. */
    private static final int MAX_QUERY_LENGTH = 100;

    /** Links maiores são recusados direto, sem chegar ao provedor. */
    private static final int MAX_LINK_LENGTH = 500;

    private static final String LINK_DESCRIPTION = "from link";

    private final MusicProvider provider;

    public List<MusicPlaylist> featuredPlaylists() throws MusicProviderException {
        return provider.featuredPlaylists();
    }

    /**
     * Playlists encontradas pelo nome. Uma busca vazia não chega ao provedor e não encontra nada.
     */
    public List<MusicPlaylist> searchPlaylists(String query) throws MusicProviderException {
        final String normalized = Objects.isNull(query) ? "" : query.strip();

        if (normalized.isEmpty()) {
            return List.of();
        }

        return provider.searchPlaylists(normalized.length() > MAX_QUERY_LENGTH ? normalized.substring(0, MAX_QUERY_LENGTH) : normalized);
    }

    public MusicPlaylist getPlaylistByLinkOrException(String link) throws MusicProviderException, PlaylistNotFoundException {
        if (Objects.isNull(link) || link.isBlank() || link.length() > MAX_LINK_LENGTH) {
            throw new PlaylistNotFoundException(LINK_DESCRIPTION);
        }

        return provider.findPlaylistByLink(link).orElseThrow(() -> new PlaylistNotFoundException(LINK_DESCRIPTION));
    }

    public MusicPlaylist getPlaylistOrException(String playlistId) throws MusicProviderException, PlaylistNotFoundException {
        return provider.findPlaylist(playlistId).orElseThrow(() -> new PlaylistNotFoundException(playlistId));
    }

    public List<MusicTrack> playlistTracks(String playlistId) throws MusicProviderException {
        return provider.playlistTracks(playlistId);
    }

    public Optional<String> findPreview(String trackId) throws MusicProviderException {
        return provider.findPreview(trackId);
    }
}
