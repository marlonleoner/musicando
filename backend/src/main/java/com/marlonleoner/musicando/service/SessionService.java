package com.marlonleoner.musicando.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final List<SessionConnectEvent> sessions = new CopyOnWriteArrayList<>();

    public void create(SessionConnectEvent session) {
        sessions.add(session);
    }

    public void delete(SessionConnectEvent session) {
        sessions.remove(session);
    }

    // public SessionConnectEvent findSessionById(String sessionId) {
    // return sessions.stream()
    // .filter(session -> session.getId().equals(sessionId))
    // .findFirst()
    // .orElse(null);
    // }
}
