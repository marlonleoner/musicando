package com.marlonleoner.musicando.handler.socket.command;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.enums.ErrorCode;
import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.domain.exception.SocketCommandException;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.domain.socket.SocketEventType;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import com.marlonleoner.musicando.service.socket.SocketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KickPlayerCommandHandlerTest {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    @Mock
    private PlayerService playerService;

    @Mock
    private SocketService socketService;

    @Mock
    private RoomStateNotifier notifier;

    @InjectMocks
    private KickPlayerCommandHandler handler;

    @Test
    void shouldNotifyKickedPlayerRemoveAndNotifyRoomWhenHostKicksPlayer() throws BaseException {
        final Player player = Fixture.make(Player.class);
        final String code = player.getRoom().getCode();
        when(playerService.getOrException(player.getId())).thenReturn(player);

        handler.handle(host(code), payload(player.getId()));

        final InOrder order = inOrder(socketService, playerService, notifier);
        order.verify(socketService).sendToParticipant(eq(player.getId()), argThat(event -> SocketEventType.KICKED.equals(event.type())));
        order.verify(playerService).delete(player);
        order.verify(playerService).ensureOwner(code);
        order.verify(notifier).broadcast(code, RoomStateReason.PLAYER_KICKED, player.getId());
    }

    @Test
    void shouldThrowObjectNotFoundExceptionWhenPlayerBelongsToAnotherRoom() throws BaseException {
        final Player player = Fixture.make(Player.class);
        when(playerService.getOrException(player.getId())).thenReturn(player);

        assertThatThrownBy(() -> handler.handle(host("OTHER1"), payload(player.getId()))).isInstanceOf(ObjectNotFoundException.class);
        verify(playerService, never()).delete(any());
        verifyNoInteractions(socketService, notifier);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"playerId\":\"\"}", "{\"other\":\"value\"}"})
    void shouldThrowInvalidCommandWhenPlayerIdIsMissing(final String json) {
        final JsonNode payload = JSON_MAPPER.readTree(json);

        assertInvalidCommand(payload);
    }

    @Test
    void shouldThrowInvalidCommandWhenPayloadIsNull() {
        assertInvalidCommand(null);
    }

    private void assertInvalidCommand(final JsonNode payload) {
        assertThatThrownBy(() -> handler.handle(host("ABC123"), payload))
                .isInstanceOf(SocketCommandException.class)
                .extracting(ex -> ((SocketCommandException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_COMMAND);
        verifyNoInteractions(playerService, socketService, notifier);
    }

    private SocketPrincipal host(final String code) {
        return new SocketPrincipal(RoleEnum.HOST, "room-id", code);
    }

    private JsonNode payload(final String playerId) {
        return JSON_MAPPER.createObjectNode().put("playerId", playerId);
    }
}
