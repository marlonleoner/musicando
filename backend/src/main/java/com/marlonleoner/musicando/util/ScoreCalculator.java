package com.marlonleoner.musicando.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Pontuação de um acerto: de {@value #MIN_POINTS} a {@value #MAX_POINTS} pontos, proporcional ao tempo que sobrava na
 * rodada. Erros e rodadas sem resposta valem 0.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScoreCalculator {

    public static final int MIN_POINTS = 500;

    public static final int MAX_POINTS = 1000;

    public static int points(final long responseTimeMs, final long roundDurationMs) {
        if (roundDurationMs <= 0) {
            return MAX_POINTS;
        }

        final long remaining = Math.clamp(roundDurationMs - responseTimeMs, 0L, roundDurationMs);

        return MIN_POINTS + (int) Math.round((double) (MAX_POINTS - MIN_POINTS) * remaining / roundDurationMs);
    }
}
