package com.marlonleoner.musicando.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RoomStatus {

    WAITING(1, "Waiting for players"),
    IN_PROGRESS(2, "Match in progress"),
    PAUSED(3, "Match paused"),
    FINISHED(4, "Match finished"),
    CLOSED(5, "Room closed");

    private final Integer id;

    private final String description;
}
