package com.marlonleoner.musicando.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BroadcastEnum {

    UPDATE_ROOM("/topic/room/%s");

    private String topic;
}