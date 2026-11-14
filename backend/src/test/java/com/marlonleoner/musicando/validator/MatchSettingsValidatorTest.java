package com.marlonleoner.musicando.validator;

import com.marlonleoner.musicando.domain.exception.InvalidSettingsException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchSettingsValidatorTest {

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 15, 20})
    void shouldAcceptRoundsWhenValueIsAnOption(final int rounds) {
        assertThatCode(() -> MatchSettingsValidator.validateRounds(rounds)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 4, 11, 25, -5})
    void shouldRejectRoundsWhenValueIsNotAnOption(final int rounds) {
        assertThatThrownBy(() -> MatchSettingsValidator.validateRounds(rounds)).isInstanceOf(InvalidSettingsException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 15, 20, 30})
    void shouldAcceptRoundDurationWhenValueIsAnOption(final int duration) {
        assertThatCode(() -> MatchSettingsValidator.validateRoundDuration(duration)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5, 25, 60})
    void shouldRejectRoundDurationWhenValueIsNotAnOption(final int duration) {
        assertThatThrownBy(() -> MatchSettingsValidator.validateRoundDuration(duration)).isInstanceOf(InvalidSettingsException.class);
    }
}
