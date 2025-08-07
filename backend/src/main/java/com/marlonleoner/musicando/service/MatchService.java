package com.marlonleoner.musicando.service;

import java.util.Date;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.marlonleoner.musicando.domain.Match;
import com.marlonleoner.musicando.domain.enums.MatchStatus;
import com.marlonleoner.musicando.repository.MatchRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MatchService {

    private final MatchRepository repository;

    private Match save(Match match) {
        match.setUpdatedAt(new Date());
        return repository.save(match);
    }

    public void onConnect(Match match) {
        if (Objects.isNull(match) || !match.isPaused())
            return;

        match.setStatus(MatchStatus.IN_PROGRESS);

        save(match);
    }

    public void onDisconnect(Match match) {
        if (Objects.isNull(match) || !match.isInProgress())
            return;

        match.setStatus(MatchStatus.PAUSED);

        save(match);
    }
}
