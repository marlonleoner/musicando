package com.marlonleoner.musicando.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

    public static final String REDIS_SESSIONS = "@MUSICANDO:SESSIONS:%s";
    public static final String REDIS_HOST_CONNECTED = "@MUSICANDO:ROOMS:%s:HOST-CONNECTED";

    public static final String EVENTS_EXCHANGER = "MUSICANDO-EXCHANGER";
    public static final String EVENTS_ROOM_QUEUE = "MUSICANDO-EVENTS-%s";

    public static final String EVENT_UPDATE_ROOM = "/topic/room/";

    public static final Integer SESSION_TTL_SECONDS = 3600;
}
