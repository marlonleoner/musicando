package com.marlonleoner.musicando.service.socket;

import com.marlonleoner.musicando.domain.event.BroadcastEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SocketServiceTest {

    private static final Map<String, String> PAYLOAD = Map.of("code", "ABC123");

    private static final String PAYLOAD_JSON = "{\"code\":\"ABC123\"}";

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private SocketService service;

    @BeforeEach
    void setUp() {
        service = new SocketService(messagingTemplate, JsonMapper.builder().build());
    }

    @Test
    void shouldSendSerializedPayloadToRoomTopicWhenBroadcasting() {
        service.broadcastAll(BroadcastEnum.UPDATE_ROOM, "ABC123", PAYLOAD);

        verify(messagingTemplate).convertAndSend("/topic/room/ABC123", PAYLOAD_JSON);
    }

    @Test
    void shouldSendSerializedPayloadToSessionQueueWhenSendingToSession() {
        service.sendToSession("session-1", PAYLOAD);

        verify(messagingTemplate).convertAndSendToUser(eq("session-1"), eq("/queue/room/host"), eq(PAYLOAD_JSON),
                any(MessageHeaders.class));
    }
}
