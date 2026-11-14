package com.marlonleoner.musicando.service.socket;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import com.marlonleoner.musicando.dto.socket.SocketErrorPayload;
import com.marlonleoner.musicando.dto.socket.SocketEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SocketServiceTest {

    private static final SocketEvent EVENT = SocketEvent.error(new SocketErrorPayload(ErrorCode.FORBIDDEN, "denied", null));

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private SocketService service;

    @BeforeEach
    void setUp() {
        service = new SocketService(messagingTemplate, jsonMapper);
    }

    @Test
    void shouldSendSerializedEventToRoomTopicWhenBroadcastingToRoom() {
        service.broadcastToRoom("ABC123", EVENT);

        assertThat(captureSent("/topic/room/ABC123").path("type").asString()).isEqualTo("ERROR");
    }

    @Test
    void shouldSendSerializedEventToHostTopicWhenSendingToHost() {
        service.sendToHost("ABC123", EVENT);

        assertThat(captureSent("/topic/room/ABC123/host").path("payload").path("code").asString()).isEqualTo("FORBIDDEN");
    }

    @Test
    void shouldSendSerializedEventToParticipantQueueWhenSendingToParticipant() {
        service.sendToParticipant("player-1", EVENT);

        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSendToUser(eq("player-1"), eq("/queue/events"), captor.capture());
        assertThat(jsonMapper.readTree((String) captor.getValue()).path("type").asString()).isEqualTo("ERROR");
    }

    @Test
    void shouldSendSerializedErrorToParticipantErrorQueueWhenSendingError() {
        service.sendErrorToParticipant("player-1", EVENT);

        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSendToUser(eq("player-1"), eq("/queue/errors"), captor.capture());
        assertThat(jsonMapper.readTree((String) captor.getValue()).path("payload").path("code").asString()).isEqualTo("FORBIDDEN");
    }

    @Test
    void shouldSerializeEventWhenConvertingToJson() {
        assertThat(jsonMapper.readTree(service.toJson(EVENT)).path("type").asString()).isEqualTo("ERROR");
    }

    private JsonNode captureSent(final String destination) {
        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(eq(destination), captor.capture());

        return jsonMapper.readTree((String) captor.getValue());
    }
}
