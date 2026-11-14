package com.marlonleoner.musicando.service.match;

import com.marlonleoner.musicando.domain.Match;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.MatchStatus;
import com.marlonleoner.musicando.domain.music.MusicPlaylist;
import com.marlonleoner.musicando.repository.MatchRepository;
import com.marlonleoner.musicando.util.UUID7;
import com.marlonleoner.musicando.validator.MatchSettingsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class MatchService {

    private final MatchRepository repository;

    private Match save(Match match) {
        match.setUpdatedAt(new Date());

        return repository.save(match);
    }

    public void onConnect(Match match) {
        if (Objects.isNull(match) || !match.isPaused()) {
            return;
        }

        match.setStatus(MatchStatus.IN_PROGRESS);

        save(match);
    }

    public void onDisconnect(Match match) {
        if (Objects.isNull(match) || !match.isInProgress()) {
            return;
        }

        match.setStatus(MatchStatus.PAUSED);

        save(match);
    }

    /**
     * Cria a partida da sala, aguardando a configuração do VIP, com rodadas e duração padrão.
     */
    public Match createFor(Room room) {
        return repository.save(build(room));
    }

    /**
     * Cria a próxima partida da sala ("jogar novamente"), já com a configuração da partida anterior.
     */
    public Match createNext(Room room, Match previous) {
        final Match match = build(room);

        if (Objects.nonNull(previous)) {
            match.setPlaylistId(previous.getPlaylistId());
            match.setPlaylistName(previous.getPlaylistName());
            match.setPlaylistCover(previous.getPlaylistCover());
            match.setPlaylistTrackCount(previous.getPlaylistTrackCount());
            match.setRounds(previous.getRounds());
            match.setRoundDuration(previous.getRoundDuration());
        }

        return repository.save(match);
    }

    private Match build(Room room) {
        final Date now = new Date();

        return Match.builder()
                .id(UUID7.generate())
                .status(MatchStatus.WAITING)
                .rounds(MatchSettingsValidator.DEFAULT_ROUNDS)
                .roundDuration(MatchSettingsValidator.DEFAULT_ROUND_DURATION)
                .room(room)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void updatePlaylist(Match match, MusicPlaylist playlist) {
        match.setPlaylistId(playlist.id());
        match.setPlaylistName(playlist.name());
        match.setPlaylistCover(playlist.cover());
        match.setPlaylistTrackCount(playlist.trackCount());
        save(match);
    }

    public void updateRounds(Match match, int rounds) {
        match.setRounds(rounds);
        save(match);
    }

    public void updateRoundDuration(Match match, int roundDuration) {
        match.setRoundDuration(roundDuration);
        save(match);
    }

    public void start(Match match) {
        match.setStatus(MatchStatus.IN_PROGRESS);
        save(match);
    }

    public void finish(Match match) {
        if (Objects.isNull(match)) {
            return;
        }

        match.setStatus(MatchStatus.FINISHED);
        match.setEndTime(new Date());
        save(match);
    }
}
