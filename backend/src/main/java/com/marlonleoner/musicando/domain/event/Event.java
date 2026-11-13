package com.marlonleoner.musicando.domain.event;

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
}
