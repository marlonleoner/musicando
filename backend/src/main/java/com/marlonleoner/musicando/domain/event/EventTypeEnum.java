package com.marlonleoner.musicando.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventTypeEnum {

    // Connections
    // HOST_CONNECTION("host/connection", HostConnectHandler.class);
    // HOST_DISCONNECT("host/disconnect", HostDisconnectHandler.class),
    // CLIENT_CONNECTION("client/connection", ClientConnectionHandler.class),
    // CLIENT_DISCONNECT("client/disconnect", ClientDisconnectHandler.class),
    // // HostEvents
    // ROUND_PRE_LIVE("round/pre-live", RoundPreLiveHandler.class),
    // ROUND_LIVE("round/live", RoundLiveHandler.class),
    // ROUND_FINISH("round/finish", RoundFinishHandler.class),
    // ROUND_SUMMARY("round/summary", RoundSummaryHandler.class),
    // GAME_FINISH("game/finish", GameFinishHandler.class),
    // // ClientEvents
    // CLIENT_SEND_ANSWER("client/send-answer", ClientAnswerHandler.class),
    // // VipEvents
    // VIP_CHANGE_AMOUNT("vip/number-rounds", VipNumberOfRoundsHandler.class),
    // VIP_CHANGE_TIMER("vip/round-duration", VipRoundDurationHandler.class),
    // VIP_CHANGE_PLAYLIST("vip/change-playlist", VipChangePlaylistHandler.class),
    // VIP_START_GAME("vip/start-game", VipStartGameHandler.class),
    // VIP_RESET_GAME("vip/reset-game", VipResetGameHandler.class);

    // private final String type;

    // private final Class<? extends AbstractHandler> handler;

    // public static EventTypeEnum fromCode(String code) {
    // return Stream.of(values())
    // .filter(item -> item.getType().equals(code))
    // .findFirst()
    // .orElse(null);
    // }
}
