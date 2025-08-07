package com.marlonleoner.musicando.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MatchStatus {

    WAITING(1, "Waiting"),
    PAUSED(2, "Paused"),
    IN_PROGRESS(3, "In Progress"),
    FINISHED(4, "Finished");

    private final Integer id;

    private final String description;
}