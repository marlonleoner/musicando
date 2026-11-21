package com.marlonleoner.musicando.service.game;

import com.marlonleoner.musicando.domain.exception.PlaylistTooSmallException;
import com.marlonleoner.musicando.domain.music.GameRound;
import com.marlonleoner.musicando.domain.music.MusicTrack;
import com.marlonleoner.musicando.util.TitleNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * Sorteia as rodadas de uma partida a partir das músicas da playlist. Cada rodada tem 1 título correto e 3 títulos
 * errados da mesma playlist, todos normalizados e distintos, em ordem aleatória. As erradas nunca são respostas de rodadas
 * anteriores (o que entregaria que não são a certa) e se espalham pela playlist, para não repetir as mesmas opções a
 * partida toda.
 */
@Component
@RequiredArgsConstructor
public class RoundGenerator {

    public static final int OPTIONS_PER_ROUND = 4;

    private final RandomGenerator random;

    /**
     * @throws PlaylistTooSmallException se a playlist não tiver títulos distintos com prévia suficientes para as
     *                                   rodadas e para as opções
     */
    public List<GameRound> generate(List<MusicTrack> tracks, int rounds) throws PlaylistTooSmallException {
        return generate(tracks, rounds, Set.of());
    }

    /**
     * Igual a {@link #generate(List, int)}, mas evita usar como resposta as músicas de {@code avoidedTrackIds} (as que
     * tocaram no lobby). Elas só viram resposta quando a playlist não tem outras suficientes, e podem aparecer como
     * opção errada.
     *
     * @throws PlaylistTooSmallException se a playlist não tiver títulos distintos com prévia suficientes para as
     *                                   rodadas e para as opções
     */
    public List<GameRound> generate(List<MusicTrack> tracks, int rounds, Set<String> avoidedTrackIds) throws PlaylistTooSmallException {
        final Map<String, MusicTrack> byTitle = new LinkedHashMap<>();
        tracks.stream()
                .filter(MusicTrack::playable)
                .forEach(track -> byTitle.putIfAbsent(TitleNormalizer.normalize(track.title()), track));
        byTitle.remove("");

        final int required = Math.max(rounds, OPTIONS_PER_ROUND);

        if (byTitle.size() < required) {
            throw new PlaylistTooSmallException(required, byTitle.size());
        }

        final List<String> titles = new ArrayList<>(byTitle.keySet());
        final List<String> answers = new ArrayList<>(titles);
        Collections.shuffle(answers, random);
        answers.sort(Comparator.comparing(title -> avoidedTrackIds.contains(byTitle.get(title).id())));

        final List<GameRound> generated = new ArrayList<>(rounds);
        final Set<String> pastAnswers = new HashSet<>();
        final Map<String, Integer> timesShown = new HashMap<>();

        for (String answer : answers.subList(0, rounds)) {
            final List<String> distractors = pickDistractors(titles, answer, pastAnswers, timesShown);
            distractors.forEach(title -> timesShown.merge(title, 1, Integer::sum));
            pastAnswers.add(answer);

            final List<String> options = new ArrayList<>(distractors);
            options.add(answer);
            Collections.shuffle(options, random);

            final MusicTrack track = byTitle.get(answer);
            generated.add(new GameRound(track.id(), answer, options, track.artist(), track.cover()));
        }

        return generated;
    }

    /**
     * Opções erradas de uma rodada: primeiro as músicas que ainda não foram resposta, das que apareceram menos vezes como
     * opção para as que apareceram mais, com os empates sorteados. Respostas de rodadas anteriores só entram quando a
     * playlist não tem outras músicas suficientes.
     */
    private List<String> pickDistractors(List<String> titles, String answer, Set<String> pastAnswers, Map<String, Integer> timesShown) {
        final List<String> candidates = new ArrayList<>(titles);
        candidates.remove(answer);
        Collections.shuffle(candidates, random);
        candidates.sort(Comparator.comparing((String title) -> pastAnswers.contains(title))
                .thenComparing(title -> timesShown.getOrDefault(title, 0)));

        return new ArrayList<>(candidates.subList(0, OPTIONS_PER_ROUND - 1));
    }
}
