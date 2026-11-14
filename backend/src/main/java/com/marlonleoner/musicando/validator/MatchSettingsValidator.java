package com.marlonleoner.musicando.validator;

import com.marlonleoner.musicando.domain.exception.InvalidSettingsException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Valores aceitos na configuração de uma partida.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MatchSettingsValidator {

    public static final List<Integer> ROUND_OPTIONS = List.of(5, 10, 15, 20);

    public static final List<Integer> ROUND_DURATION_OPTIONS = List.of(10, 15, 20, 30);

    public static final int DEFAULT_ROUNDS = 10;

    public static final int DEFAULT_ROUND_DURATION = 20;

    public static void validateRounds(final int rounds) throws InvalidSettingsException {
        if (!ROUND_OPTIONS.contains(rounds)) {
            throw new InvalidSettingsException("Rounds must be one of " + ROUND_OPTIONS);
        }
    }

    public static void validateRoundDuration(final int roundDuration) throws InvalidSettingsException {
        if (!ROUND_DURATION_OPTIONS.contains(roundDuration)) {
            throw new InvalidSettingsException("Round duration must be one of " + ROUND_DURATION_OPTIONS);
        }
    }
}
