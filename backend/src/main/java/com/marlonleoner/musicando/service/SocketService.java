package com.marlonleoner.musicando.service;

import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.marlonleoner.musicando.domain.event.BroadcastEnum;
import com.marlonleoner.musicando.mapper.JSONMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        String destination = String.format(context.getTopic(), code);
        String message = JSONMapper.serialize(payload);
        log.info("Broadcasting message '{}': {}", destination, message);
        messagingTemplate.convertAndSend(destination, message);
    }

    public void sendToSession(String sessionId, Object payload) {
        log.info("Sending message to session {}: {}", sessionId, payload);
        messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/room/host",
                JSONMapper.serialize(payload),
                createHeaders(sessionId));
    }

}
