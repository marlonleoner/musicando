package com.marlonleoner.musicando.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SocketDestinationsTest {

    @Test
    void shouldBuildRoomTopicWhenCodeIsGiven() {
        assertThat(SocketDestinations.roomTopic("ABC123")).isEqualTo("/topic/room/ABC123");
    }

    @Test
    void shouldBuildHostTopicWhenCodeIsGiven() {
        assertThat(SocketDestinations.roomHostTopic("ABC123")).isEqualTo("/topic/room/ABC123/host");
    }

    @Test
    void shouldExposeFullDestinationsWhenUsedByClients() {
        assertThat(SocketDestinations.COMMAND).isEqualTo("/musicando/command");
        assertThat(SocketDestinations.USER_EVENTS_QUEUE).isEqualTo("/user/queue/events");
        assertThat(SocketDestinations.USER_ERRORS_QUEUE).isEqualTo("/user/queue/errors");
    }
}
