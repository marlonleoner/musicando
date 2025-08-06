package com.marlonleoner.musicando.domain.socket;

import java.util.stream.Stream;

import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.handler.socket.AbstractSocketHandler;
import com.marlonleoner.musicando.handler.socket.ClientConnectHandler;
import com.marlonleoner.musicando.handler.socket.ClientDisconnectHandler;
import com.marlonleoner.musicando.handler.socket.HostConnectHandler;
import com.marlonleoner.musicando.handler.socket.HostDisconnectHandler;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ConnectionTypeEnum {

    HOST_CONNECTION("host/connection", HostConnectHandler.class),
    HOST_DISCONNECT("host/disconnect", HostDisconnectHandler.class),
    CLIENT_CONNECTION("client/connection", ClientConnectHandler.class),
    CLIENT_DISCONNECT("client/disconnect", ClientDisconnectHandler.class);

    private final String type;

    private final Class<? extends AbstractSocketHandler> handler;

    public static ConnectionTypeEnum get(RoleEnum role, boolean connect) {
        String type = role.toString() + (connect ? "/connection" : "/disconnect");
        return Stream.of(values())
                .filter(item -> item.getType().equals(type))
                .findFirst()
                .orElse(null);
    }

    // HOST_CONNECTION("host/connection", SocketHandler::onHostConnection),
    // HOST_DISCONNECT("host/disconnect", SocketHandler::onHostDisconnect),
    // PLAYER_CONNECTION("player/connection", SocketHandler::onPlayerConnection),
    // PLAYER_DISCONNECT("player/disconnect", SocketHandler::onPlayerDisconnect);

    // private final String role;

    // private final Consumer<SocketAttributes> action;

    // ConnectionTypeEnum(String role, Consumer<SocketAttributes> action) {
    // this.role = role;
    // this.action = action;
    // }

    // public void execute(SocketAttributes payload) {
    // action.accept(payload);
    // }

    // public static ConnectionTypeEnum get(String value) {
    // return Stream.of(values())
    // .filter(type -> type.getRole().equals(value))
    // .findFirst()
    // .orElseThrow(() -> new IllegalArgumentException("Invalid connection type: " +
    // value));
    // }
}
