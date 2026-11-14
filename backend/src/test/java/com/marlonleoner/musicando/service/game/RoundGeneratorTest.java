package com.marlonleoner.musicando.service.game;

import com.marlonleoner.musicando.domain.exception.PlaylistTooSmallException;
import com.marlonleoner.musicando.domain.music.GameRound;
import com.marlonleoner.musicando.domain.music.MusicTrack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoundGeneratorTest {

    private final RoundGenerator generator = new RoundGenerator(new Random(42));

    private static List<MusicTrack> tracks(final int amount) {
        return IntStream.rangeClosed(1, amount).mapToObj(i -> new MusicTrack(String.valueOf(i), "Song " + i, "Artist", true)).toList();
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 20})
    void shouldGenerateRequestedRoundsWithDistinctAnswersWhenPlaylistIsLargeEnough(final int rounds) throws PlaylistTooSmallException {
        final List<GameRound> generated = generator.generate(tracks(25), rounds);

        assertThat(generated).hasSize(rounds);
        assertThat(generated).extracting(GameRound::answer).doesNotHaveDuplicates();
        assertThat(generated).extracting(GameRound::trackId).doesNotHaveDuplicates();
        assertThat(generated).allSatisfy(round -> {
            assertThat(round.options()).hasSize(RoundGenerator.OPTIONS_PER_ROUND).doesNotHaveDuplicates().contains(round.answer());
            assertThat(round.answer()).isEqualTo("SONG " + round.trackId());
        });
    }

    @Test
    void shouldUseAllTitlesAsAnswersWhenRoundsEqualDistinctTitles() throws PlaylistTooSmallException {
        final List<GameRound> generated = generator.generate(tracks(5), 5);

        assertThat(generated).extracting(GameRound::answer).containsExactlyInAnyOrder("SONG 1", "SONG 2", "SONG 3", "SONG 4", "SONG 5");
    }

    @Test
    void shouldIgnoreTracksWithoutPreviewWhenGeneratingRounds() throws PlaylistTooSmallException {
        final List<MusicTrack> tracks = List.of(
                new MusicTrack("1", "A", null, true),
                new MusicTrack("2", "B", null, true),
                new MusicTrack("3", "C", null, true),
                new MusicTrack("4", "D", null, true),
                new MusicTrack("5", "E", null, false));

        final List<GameRound> generated = generator.generate(tracks, 4);

        assertThat(generated).flatExtracting(GameRound::options).doesNotContain("E");
    }

    @Test
    void shouldKeepFirstTrackWhenTitlesAreDuplicatedAfterNormalization() {
        final List<MusicTrack> tracks = List.of(
                new MusicTrack("1", "Song", null, true),
                new MusicTrack("2", "Song (Live)", null, true),
                new MusicTrack("3", "song - Remastered", null, true),
                new MusicTrack("4", "Other", null, true),
                new MusicTrack("5", "Another", null, true));

        assertThatThrownBy(() -> generator.generate(tracks, 4)).isInstanceOf(PlaylistTooSmallException.class);
    }

    @Test
    void shouldThrowPlaylistTooSmallExceptionWhenThereAreFewerTitlesThanRounds() {
        assertThatThrownBy(() -> generator.generate(tracks(9), 10)).isInstanceOf(PlaylistTooSmallException.class);
    }

    @Test
    void shouldThrowPlaylistTooSmallExceptionWhenThereAreFewerTitlesThanOptions() {
        assertThatThrownBy(() -> generator.generate(tracks(3), 1)).isInstanceOf(PlaylistTooSmallException.class);
    }

    @Test
    void shouldIgnoreTracksWithBlankTitleWhenGeneratingRounds() {
        final List<MusicTrack> tracks = List.of(
                new MusicTrack("1", "A", null, true),
                new MusicTrack("2", "B", null, true),
                new MusicTrack("3", "C", null, true),
                new MusicTrack("4", "  ", null, true));

        assertThatThrownBy(() -> generator.generate(tracks, 4)).isInstanceOf(PlaylistTooSmallException.class);
    }
}
