package com.marlonleoner.musicando.mapper;

import com.marlonleoner.musicando.domain.Match;
import com.marlonleoner.musicando.dto.response.MatchSettingsResponse;
import com.marlonleoner.musicando.dto.response.PlaylistResponse;
import org.mapstruct.Mapper;

import java.util.Objects;

@Mapper
public interface MatchMapper {

    default MatchSettingsResponse toSettings(Match match) {
        if (Objects.isNull(match)) {
            return null;
        }

        final PlaylistResponse playlist = match.hasPlaylist()
                ? new PlaylistResponse(match.getPlaylistId(), match.getPlaylistName(), match.getPlaylistCover(),
                Objects.requireNonNullElse(match.getPlaylistTrackCount(), 0))
                : null;

        return new MatchSettingsResponse(playlist, match.getRounds(), match.getRoundDuration());
    }
}
