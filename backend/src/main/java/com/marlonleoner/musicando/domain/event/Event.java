package com.marlonleoner.musicando.domain.event;

import com.marlonleoner.musicando.domain.socket.SocketAttributes;
import com.marlonleoner.musicando.mapper.JSONMapper;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Event {

    private String sessionId;

    private String type;

    private String code;

    private String playerId;

    private String object;

    public Event(SocketAttributes connection) {
        String connectionRole = connection.role().toString();
        String eventType = connectionRole.concat("/connection");

        this.sessionId = connection.sessionId();
        this.type = eventType;
        this.code = connection.code();
        this.playerId = sessionId;
        this.object = JSONMapper.serialize(connection);
    }

    // public static Event instanceRoundPreLiveEvent(String gameId) {
    // Event event = new Event();
    // event.setGameId(gameId);
    // event.setType(EventTypeEnum.ROUND_PRE_LIVE.getType());

    // return event;
    // }

    // public static Event instanceRoundLiveEvent(String gameId, Round round) {
    // Event event = new Event();
    // event.setGameId(gameId);
    // event.setType(EventTypeEnum.ROUND_LIVE.getType());
    // event.setObject(round);

    // return event;
    // }

    // public static Event instanceRoundFinishEvent(String gameId, Round round) {
    // Event event = new Event();
    // event.setGameId(gameId);
    // event.setType(EventTypeEnum.ROUND_FINISH.getType());
    // event.setObject(round);

    // return event;
    // }

    // public static Event instanceRoundSummaryEvent(String gameId, Round round) {
    // Event event = new Event();
    // event.setGameId(gameId);
    // event.setType(EventTypeEnum.ROUND_SUMMARY.getType());
    // event.setObject(round);

    // return event;
    // }

    // public static Event instanceGameFinishedEvent(String gameId) {
    // Event event = new Event();
    // event.setGameId(gameId);
    // event.setType(EventTypeEnum.GAME_FINISH.getType());

    // return event;
    // }

    // public static Event instanceDisconnectEvent(ConnectionSocket connection) {
    // String connectionRole = connection.getRole().getDescription();
    // String eventType = connectionRole.concat("/disconnect");

    // Event event = new Event();
    // event.setSessionId(connection.getSessionId());
    // event.setType(eventType);
    // event.setGameId(connection.getGameId());
    // event.setRole(connectionRole);
    // event.setObject(connection);

    // return event;
    // }
}