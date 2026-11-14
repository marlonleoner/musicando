package com.marlonleoner.musicando.mapper;

import com.marlonleoner.musicando.domain.Match;
import com.marlonleoner.musicando.dto.response.MatchSettingsResponse;
import com.marlonleoner.musicando.fixture.Fixture;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MatchMapperTest {

    private final MatchMapper mapper = new MatchMapperImpl();

    @Test
    void shouldMapPlaylistAndSettingsWhenMatchHasPlaylist() {
        final Match match = Fixture.make(Match.class);

        final MatchSettingsResponse settings = mapper.toSettings(match);

        assertThat(settings.playlist().id()).isEqualTo(match.getPlaylistId());
        assertThat(settings.playlist().name()).isEqualTo(match.getPlaylistName());
        assertThat(settings.playlist().cover()).isEqualTo(match.getPlaylistCover());
        assertThat(settings.playlist().trackCount()).isEqualTo(match.getPlaylistTrackCount());
        assertThat(settings.rounds()).isEqualTo(match.getRounds());
        assertThat(settings.roundDuration()).isEqualTo(match.getRoundDuration());
    }

    @Test
    void shouldReturnNullPlaylistWhenPlaylistWasNotChosen() {
        final Match match = Fixture.make(Match.class);
        match.setPlaylistId(null);

        assertThat(mapper.toSettings(match).playlist()).isNull();
    }

    @Test
    void shouldUseZeroTracksWhenTrackCountIsUnknown() {
        final Match match = Fixture.make(Match.class);
        match.setPlaylistTrackCount(null);

        assertThat(mapper.toSettings(match).playlist().trackCount()).isZero();
    }

    @Test
    void shouldReturnNullWhenMatchIsNull() {
        assertThat(mapper.toSettings(null)).isNull();
    }
}
