package com.marlonleoner.musicando.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class LogSanitizerTest {

    @Test
    void shouldReturnNullWhenValueIsNull() {
        assertThat(LogSanitizer.sanitize(null)).isNull();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "ABC123|ABC123",
        "linha1\\nlinha2|linha1_linha2",
        "linha1\\rlinha2|linha1_linha2",
        "linha1\\r\\nlinha2|linha1__linha2",
    })
    void shouldReplaceLineBreaksWhenValueContainsThem(final String value, final String expected) {
        final String input = value.replace("\\r", "\r").replace("\\n", "\n");

        assertThat(LogSanitizer.sanitize(input)).isEqualTo(expected);
    }

    @Test
    void shouldUseStringValueWhenValueIsNotString() {
        assertThat(LogSanitizer.sanitize(42)).isEqualTo("42");
    }
}
