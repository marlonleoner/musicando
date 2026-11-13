package com.marlonleoner.musicando.service.match;

import com.marlonleoner.musicando.domain.Match;
import com.marlonleoner.musicando.domain.enums.MatchStatus;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.repository.MatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
}
