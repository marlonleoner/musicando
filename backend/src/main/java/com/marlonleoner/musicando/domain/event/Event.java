package com.marlonleoner.musicando.domain.event;

import com.marlonleoner.musicando.domain.socket.SocketAttributes;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Event {

    private String sessionId;

    private String type;

    private String code;

    private String playerId;

    private Object object;

    public Event(SocketAttributes connection) {
        String connectionRole = connection.role().toString();
        String eventType = connectionRole.concat("/connection");

        this.sessionId = connection.sessionId();
        this.type = eventType;
        this.code = connection.code();
        this.playerId = sessionId;
        this.object = connection;
    }
}
