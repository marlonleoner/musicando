package com.marlonleoner.musicando.service.match;

import com.marlonleoner.musicando.domain.Match;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.MatchStatus;
import com.marlonleoner.musicando.domain.music.MusicPlaylist;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.repository.MatchRepository;
import com.marlonleoner.musicando.validator.MatchSettingsValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private MatchRepository repository;

    @InjectMocks
    private MatchService service;

    @Test
    void shouldResumeMatchWhenHostConnectsAndMatchIsPaused() {
        final Match match = Fixture.make(Match.class);
        match.setStatus(MatchStatus.PAUSED);

        service.onConnect(match);

        verify(repository).save(match);
        assertThat(match.getStatus()).isEqualTo(MatchStatus.IN_PROGRESS);
        assertThat(match.getUpdatedAt()).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(value = MatchStatus.class, names = "PAUSED", mode = EnumSource.Mode.EXCLUDE)
    void shouldKeepMatchWhenHostConnectsAndMatchIsNotPaused(final MatchStatus status) {
        final Match match = Fixture.make(Match.class);
        match.setStatus(status);

        service.onConnect(match);

        verifyNoInteractions(repository);
        assertThat(match.getStatus()).isEqualTo(status);
    }

    @Test
    void shouldPauseMatchWhenHostDisconnectsAndMatchIsInProgress() {
        final Match match = Fixture.make(Match.class);
        match.setStatus(MatchStatus.IN_PROGRESS);

        service.onDisconnect(match);

        verify(repository).save(match);
        assertThat(match.getStatus()).isEqualTo(MatchStatus.PAUSED);
    }

    @ParameterizedTest
    @EnumSource(value = MatchStatus.class, names = "IN_PROGRESS", mode = EnumSource.Mode.EXCLUDE)
    void shouldKeepMatchWhenHostDisconnectsAndMatchIsNotInProgress(final MatchStatus status) {
        final Match match = Fixture.make(Match.class);
        match.setStatus(status);

        service.onDisconnect(match);

        verifyNoInteractions(repository);
        assertThat(match.getStatus()).isEqualTo(status);
    }

    @Test
    void shouldDoNothingWhenRoomHasNoMatch() {
        service.onConnect(null);
        service.onDisconnect(null);

        verifyNoInteractions(repository);
    }

    @Test
    void shouldCreateWaitingMatchWithDefaultSettingsWhenCreatingForRoom() {
        final Room room = Fixture.make(Room.class);
        when(repository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final Match match = service.createFor(room);

        assertThat(match.getId()).isNotBlank();
        assertThat(match.getStatus()).isEqualTo(MatchStatus.WAITING);
        assertThat(match.getRounds()).isEqualTo(MatchSettingsValidator.DEFAULT_ROUNDS);
        assertThat(match.getRoundDuration()).isEqualTo(MatchSettingsValidator.DEFAULT_ROUND_DURATION);
        assertThat(match.getRoom()).isEqualTo(room);
        assertThat(match.hasPlaylist()).isFalse();
        assertThat(match.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldStorePlaylistWhenUpdatingPlaylist() {
        final Match match = Fixture.make(Match.class);
        final MusicPlaylist playlist = new MusicPlaylist("123", "Hits", "cover.jpg", 50);

        service.updatePlaylist(match, playlist);

        verify(repository).save(match);
        assertThat(match.getPlaylistId()).isEqualTo("123");
        assertThat(match.getPlaylistName()).isEqualTo("Hits");
        assertThat(match.getPlaylistCover()).isEqualTo("cover.jpg");
        assertThat(match.getPlaylistTrackCount()).isEqualTo(50);
    }

    @Test
    void shouldStoreRoundsWhenUpdatingRounds() {
        final Match match = Fixture.make(Match.class);

        service.updateRounds(match, 15);

        verify(repository).save(match);
        assertThat(match.getRounds()).isEqualTo(15);
    }

    @Test
    void shouldStoreRoundDurationWhenUpdatingRoundDuration() {
        final Match match = Fixture.make(Match.class);

        service.updateRoundDuration(match, 30);

        verify(repository).save(match);
        assertThat(match.getRoundDuration()).isEqualTo(30);
    }

    @Test
    void shouldPutMatchInProgressWhenStarting() {
        final Match match = Fixture.make(Match.class);
        match.setStatus(MatchStatus.WAITING);

        service.start(match);

        verify(repository).save(match);
        assertThat(match.getStatus()).isEqualTo(MatchStatus.IN_PROGRESS);
    }

    @Test
    void shouldCopyPreviousSettingsWhenCreatingNextMatch() {
        final Room room = Fixture.make(Room.class);
        final Match previous = Fixture.make(Match.class);
        when(repository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final Match match = service.createNext(room, previous);

        assertThat(match.getId()).isNotEqualTo(previous.getId());
        assertThat(match.getStatus()).isEqualTo(MatchStatus.WAITING);
        assertThat(match.getRoom()).isEqualTo(room);
        assertThat(match.getPlaylistId()).isEqualTo(previous.getPlaylistId());
        assertThat(match.getPlaylistName()).isEqualTo(previous.getPlaylistName());
        assertThat(match.getPlaylistCover()).isEqualTo(previous.getPlaylistCover());
        assertThat(match.getPlaylistTrackCount()).isEqualTo(previous.getPlaylistTrackCount());
        assertThat(match.getRounds()).isEqualTo(previous.getRounds());
        assertThat(match.getRoundDuration()).isEqualTo(previous.getRoundDuration());
    }

    @Test
    void shouldUseDefaultSettingsWhenCreatingNextMatchWithoutPrevious() {
        when(repository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final Match match = service.createNext(Fixture.make(Room.class), null);

        assertThat(match.hasPlaylist()).isFalse();
        assertThat(match.getRounds()).isEqualTo(MatchSettingsValidator.DEFAULT_ROUNDS);
    }

    @Test
    void shouldFinishMatchWithEndTimeWhenFinishing() {
        final Match match = Fixture.make(Match.class);
        match.setStatus(MatchStatus.IN_PROGRESS);
        match.setEndTime(null);

        service.finish(match);

        verify(repository).save(match);
        assertThat(match.getStatus()).isEqualTo(MatchStatus.FINISHED);
        assertThat(match.getEndTime()).isNotNull();
    }

    @Test
    void shouldIgnoreFinishWhenMatchIsNull() {
        service.finish(null);

        verifyNoInteractions(repository);
    }

    @Test
    void shouldKeepEndTimeWhenMatchIsAlreadyFinished() {
        final Match match = Fixture.make(Match.class);
        match.setStatus(MatchStatus.FINISHED);

        service.finish(match);

        verifyNoInteractions(repository);
    }
}
