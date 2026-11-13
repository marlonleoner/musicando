package com.marlonleoner.musicando.dto.response;

import com.marlonleoner.musicando.domain.enums.RoomStatus;

import java.util.List;
import java.util.Objects;

/**
 * Estado da sala enviado a todos os inscritos no tópico da sala. Não contém segredos nem referências cíclicas.
 */
public record RoomStateBroadcast(String code, RoomStatus status, boolean connected, List<PlayerResponse> players) {
    public RoomStateBroadcast {
        players = Objects.isNull(players) ? List.of() : List.copyOf(players);
    }
}
