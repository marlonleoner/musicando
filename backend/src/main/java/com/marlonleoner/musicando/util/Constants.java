package com.marlonleoner.musicando.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

    public static final String REDIS_SESSIONS = "@MUSICANDO:SESSIONS:%s";

    public static final String EVENTS_EXCHANGER = "MUSICANDO-EXCHANGER";
    public static final String EVENTS_ROOM_QUEUE = "MUSICANDO-EVENTS-%s";
}
