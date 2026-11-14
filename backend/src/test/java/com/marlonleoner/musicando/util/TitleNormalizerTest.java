package com.marlonleoner.musicando.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;

import static org.assertj.core.api.Assertions.assertThat;

class TitleNormalizerTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "Hello|HELLO",
        "  Garota   de  Ipanema |GAROTA DE IPANEMA",
        "Song (Remastered 2011)|SONG",
        "Song - Ao Vivo|SONG",
        "Song [feat. Artist]|SONG",
        "Song (Ao Vivo) [Remastered]|SONG",
        "Song - Remastered 2009 (Live)|SONG",
        "(I Can't Get No) Satisfaction|(I CAN'T GET NO) SATISFACTION",
        "(I Can't Get No) Satisfaction (Mono Version)|(I CAN'T GET NO) SATISFACTION",
        "(Live)|(LIVE)",
        "Anna Júlia|ANNA JÚLIA",
    })
    void shouldNormalizeTitleWhenTitleHasVersionInformation(final String title, final String expected) {
        assertThat(TitleNormalizer.normalize(title)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullSource
    void shouldReturnEmptyWhenTitleIsNull(final String title) {
        assertThat(TitleNormalizer.normalize(title)).isEmpty();
    }
}
