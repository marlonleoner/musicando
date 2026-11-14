package com.marlonleoner.musicando.domain.socket;

import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.handler.socket.command.CloseRoomCommandHandler;
import com.marlonleoner.musicando.handler.socket.command.GetRoomStateCommandHandler;
import com.marlonleoner.musicando.handler.socket.command.KickPlayerCommandHandler;
import com.marlonleoner.musicando.handler.socket.command.LeaveRoomCommandHandler;
import com.marlonleoner.musicando.handler.socket.command.SocketCommandHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * Comandos aceitos em {@code /musicando/command}, com o handler responsável e os papéis autorizados a enviá-los.
 */
@Getter
@AllArgsConstructor
public enum SocketCommandType {

    GET_ROOM_STATE(Set.of(RoleEnum.HOST, RoleEnum.PLAYER), GetRoomStateCommandHandler.class),
    LEAVE_ROOM(Set.of(RoleEnum.PLAYER), LeaveRoomCommandHandler.class),
    KICK_PLAYER(Set.of(RoleEnum.HOST), KickPlayerCommandHandler.class),
    CLOSE_ROOM(Set.of(RoleEnum.HOST), CloseRoomCommandHandler.class);

    private final Set<RoleEnum> allowedRoles;

    private final Class<? extends SocketCommandHandler> handler;

    public static Optional<SocketCommandType> from(final String value) {
        return Arrays.stream(values())
                .filter(type -> type.name().equals(value))
                .findFirst();
    }

    public boolean isAllowedFor(final RoleEnum role) {
        return this.allowedRoles.contains(role);
    }
}
