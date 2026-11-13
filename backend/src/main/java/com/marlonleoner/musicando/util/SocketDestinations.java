package com.marlonleoner.musicando.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Destinos STOMP usados entre o backend e os clientes (host e jogadores).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SocketDestinations {

    public static final String APPLICATION_PREFIX = "/musicando";

    public static final String USER_PREFIX = "/user";

    public static final String COMMAND_MAPPING = "/command";

    public static final String COMMAND = APPLICATION_PREFIX + COMMAND_MAPPING;

    public static final String ROOM_SNAPSHOT_MAPPING = "/room";

    public static final String ROOM_SNAPSHOT = APPLICATION_PREFIX + ROOM_SNAPSHOT_MAPPING;

    public static final String EVENTS_QUEUE = "/queue/events";

    public static final String ERRORS_QUEUE = "/queue/errors";

    public static final String USER_EVENTS_QUEUE = USER_PREFIX + EVENTS_QUEUE;

    public static final String USER_ERRORS_QUEUE = USER_PREFIX + ERRORS_QUEUE;

    private static final String ROOM_TOPIC_PREFIX = "/topic/room/";

    private static final String HOST_SUFFIX = "/host";

    public static String roomTopic(final String code) {
        return ROOM_TOPIC_PREFIX + code;
    }

    public static String roomHostTopic(final String code) {
        return roomTopic(code) + HOST_SUFFIX;
    }
}
