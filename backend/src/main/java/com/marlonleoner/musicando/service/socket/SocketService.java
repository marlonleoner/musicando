package com.marlonleoner.musicando.service.socket;

import com.marlonleoner.musicando.domain.event.BroadcastEnum;
import com.marlonleoner.musicando.mapper.JSONMapper;
import com.marlonleoner.musicando.util.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocketService {

    private final SimpMessagingTemplate messagingTemplate;

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);

        return headerAccessor.getMessageHeaders();
    }

    public void broadcastAll(BroadcastEnum context, String code, Object payload) {
        String destination = context.getTopic() + code;
        String message = JSONMapper.serialize(payload);
        log.info("Broadcasting message '{}': {}", LogSanitizer.sanitize(destination), LogSanitizer.sanitize(message));
        messagingTemplate.convertAndSend(destination, message);
    }

    public void sendToSession(String sessionId, Object payload) {
        log.info("Sending message to session {}: {}", LogSanitizer.sanitize(sessionId), LogSanitizer.sanitize(payload));
        messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/room/host",
                JSONMapper.serialize(payload),
                createHeaders(sessionId));
    }
}
