package com.marlonleoner.musicando.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreCalculatorTest {

    @ParameterizedTest
    @CsvSource({
        "0, 20000, 1000",
        "10000, 20000, 750",
        "5000, 20000, 875",
        "20000, 20000, 500",
        "25000, 20000, 500",
        "-100, 20000, 1000",
        "100, 0, 1000",
        "1, 3, 833",
    })
    void shouldScoreBetweenMinAndMaxProportionalToRemainingTimeWhenAnswerIsCorrect(final long response, final long duration, final int expected) {
        assertThat(ScoreCalculator.points(response, duration)).isEqualTo(expected);
    }
}
