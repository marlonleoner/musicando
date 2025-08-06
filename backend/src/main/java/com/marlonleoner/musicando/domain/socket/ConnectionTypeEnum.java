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
}
