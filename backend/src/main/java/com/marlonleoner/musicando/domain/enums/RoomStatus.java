package com.marlonleoner.musicando.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RoomStatus {

    WAITING(1, "Waiting"),
    IN_PROGRESS(2, "In Progress"),
    FINISHED(3, "Finished");

    private final Integer id;

    private final String description;
}
