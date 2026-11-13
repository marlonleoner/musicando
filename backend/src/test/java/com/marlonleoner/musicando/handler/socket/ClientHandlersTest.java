package com.marlonleoner.musicando.handler.socket;

import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.socket.SocketAttributes;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class ClientHandlersTest {

    private static final SocketAttributes ATTRIBUTES = new SocketAttributes("session-1", "player-id", "ABC123", "player-secret", RoleEnum.PLAYER);

    @Test
    void shouldHandleWithoutErrorsWhenClientConnects() {
        assertThatCode(() -> new ClientConnectHandler().handle(ATTRIBUTES)).doesNotThrowAnyException();
    }

    @Test
    void shouldHandleWithoutErrorsWhenClientDisconnects() {
        assertThatCode(() -> new ClientDisconnectHandler().handle(ATTRIBUTES)).doesNotThrowAnyException();
    }
}
