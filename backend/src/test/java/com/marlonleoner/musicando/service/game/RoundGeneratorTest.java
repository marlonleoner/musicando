package com.marlonleoner.musicando.service.game;

import com.marlonleoner.musicando.domain.exception.PlaylistTooSmallException;
import com.marlonleoner.musicando.domain.music.GameRound;
import com.marlonleoner.musicando.domain.music.MusicTrack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
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

    @Test
    void shouldKeepArtistAndCoverOfAnswerWhenGeneratingRounds() throws PlaylistTooSmallException {
        final List<MusicTrack> tracks = IntStream.rangeClosed(1, 4)
                .mapToObj(i -> new MusicTrack(String.valueOf(i), "Song " + i, "Artist " + i, "cover-" + i + ".jpg", true))
                .toList();

        final List<GameRound> generated = generator.generate(tracks, 4);

        assertThat(generated).allSatisfy(round -> {
            assertThat(round.artist()).isEqualTo("Artist " + round.trackId());
            assertThat(round.cover()).isEqualTo("cover-" + round.trackId() + ".jpg");
        });
    }

    @Test
    void shouldAvoidLobbyTracksAsAnswersWhenPlaylistHasEnoughOtherTracks() throws PlaylistTooSmallException {
        final Set<String> avoided = Set.of("1", "2", "3", "4", "5");

        final List<GameRound> generated = generator.generate(tracks(15), 10, avoided);

        assertThat(generated).hasSize(10).extracting(GameRound::trackId).doesNotContainAnyElementsOf(avoided);
    }

    @Test
    void shouldUseLobbyTracksAsLastResortWhenPlaylistHasNoOtherTracks() throws PlaylistTooSmallException {
        final Set<String> avoided = Set.of("1", "2", "3");

        final List<GameRound> generated = generator.generate(tracks(5), 5, avoided);

        assertThat(generated).extracting(GameRound::trackId).containsExactlyInAnyOrder("1", "2", "3", "4", "5");
        assertThat(generated.subList(0, 2)).extracting(GameRound::trackId).containsExactlyInAnyOrder("4", "5");
    }

    private static List<String> distractorsOf(final GameRound round) {
        final List<String> distractors = new ArrayList<>(round.options());
        distractors.remove(round.answer());

        return distractors;
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 7, 42, 2024, 99_999})
    void shouldNeverOfferPreviousAnswerWhenPlaylistHasEnoughTitles(final long seed) throws PlaylistTooSmallException {
        final List<GameRound> generated = new RoundGenerator(new Random(seed)).generate(tracks(13), 10);
        final Set<String> previousAnswers = new HashSet<>();

        for (GameRound round : generated) {
            assertThat(distractorsOf(round)).noneMatch(previousAnswers::contains);
            previousAnswers.add(round.answer());
        }
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 7, 42, 2024, 99_999})
    void shouldNotRepeatWrongOptionsWhenPlaylistHasEnoughTitles(final long seed) throws PlaylistTooSmallException {
        final List<GameRound> generated = new RoundGenerator(new Random(seed)).generate(tracks(40), 10);

        assertThat(generated).flatExtracting(RoundGeneratorTest::distractorsOf).hasSize(30).doesNotHaveDuplicates();
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 7, 42, 2024, 99_999})
    void shouldOfferPreviousAnswersOnlyAsLastResortWhenPlaylistIsSmall(final long seed) throws PlaylistTooSmallException {
        final List<GameRound> generated = new RoundGenerator(new Random(seed)).generate(tracks(5), 5);
        final Set<String> previousAnswers = new HashSet<>();

        for (int index = 0; index < generated.size(); index++) {
            final GameRound round = generated.get(index);
            final long reused = distractorsOf(round).stream().filter(previousAnswers::contains).count();

            assertThat(round.options()).hasSize(RoundGenerator.OPTIONS_PER_ROUND).doesNotHaveDuplicates();
            assertThat(reused).isEqualTo(Math.max(0, index - 1));
            previousAnswers.add(round.answer());
        }
    }
}
