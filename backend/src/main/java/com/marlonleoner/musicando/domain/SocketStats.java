package com.marlonleoner.musicando.domain;

import org.springframework.web.socket.config.WebSocketMessageBrokerStats;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler.Stats;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SocketStats {

    private Integer totalSessions;

    private String raw;

    private String rawSession;

    public SocketStats(WebSocketMessageBrokerStats stats) {
        Stats sessionStats = stats.getWebSocketSessionStats();

        this.raw = stats.toString();
        this.rawSession = sessionStats.toString();

        this.totalSessions = sessionStats.getTotalSessions();
    }
}
