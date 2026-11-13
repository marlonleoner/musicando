package com.marlonleoner.musicando.domain.event;

import com.marlonleoner.musicando.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BroadcastEnum {

    UPDATE_ROOM(Constants.EVENT_UPDATE_ROOM);

    private final String topic;
}
