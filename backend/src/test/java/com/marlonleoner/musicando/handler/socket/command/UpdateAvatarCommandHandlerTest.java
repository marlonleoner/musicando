package com.marlonleoner.musicando.handler.socket.command;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.enums.RoomStatus;
import com.marlonleoner.musicando.domain.exception.AvatarTakenException;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.GameAlreadyStartedException;
import com.marlonleoner.musicando.domain.exception.InvalidAvatarException;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.service.game.GameLocks;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateAvatarCommandHandlerTest {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    @Mock
    private PlayerService playerService;

    @Mock
    private RoomService roomService;

    @Mock
    private RoomStateNotifier notifier;

    private UpdateAvatarCommandHandler handler;

    private Room room;

    private Player player;

    private SocketPrincipal principal;

    @BeforeEach
    void setUp() {
        handler = new UpdateAvatarCommandHandler(playerService, roomService, notifier, new GameLocks());
        room = Fixture.make(Room.class);
        room.setStatus(RoomStatus.WAITING);
        player = Fixture.make(Player.class);
        player.setAvatar("alien");
        principal = new SocketPrincipal(RoleEnum.PLAYER, player.getId(), room.getCode());
    }

    private static JsonNode payload(final String avatar) {
        return JSON_MAPPER.readTree("{\"avatar\":\"" + avatar + "\"}");
    }

    private void givenRoomWith(final List<Player> players) throws BaseException {
        when(roomService.getOrException(room.getCode())).thenReturn(room);
        when(playerService.getOrException(player.getId())).thenReturn(player);
        when(playerService.findByRoom(room.getCode())).thenReturn(players);
    }

    @Test
    void shouldUpdateAvatarAndNotifyRoomWhenAvatarIsFree() throws BaseException {
        final Player other = Fixture.make(Player.class);
        other.setAvatar("zombie");
        givenRoomWith(List.of(player, other));

        handler.handle(principal, payload("robot"));

        verify(playerService).updateAvatar(player, "robot");
        verify(notifier).broadcast(room.getCode(), RoomStateReason.AVATAR_UPDATED, player.getId());
    }

    @Test
    void shouldKeepAvatarWhenPlayerChoosesTheOneAlreadyInUse() throws BaseException {
        givenRoomWith(List.of(player));

        handler.handle(principal, payload("alien"));

        verify(playerService).updateAvatar(player, "alien");
        verify(notifier).broadcast(room.getCode(), RoomStateReason.AVATAR_UPDATED, player.getId());
    }

    @Test
    void shouldThrowAvatarTakenExceptionWhenAnotherPlayerUsesTheAvatar() throws BaseException {
        final Player other = Fixture.make(Player.class);
        other.setAvatar("robot");
        givenRoomWith(List.of(player, other));
        final JsonNode payload = payload("robot");

        assertThatThrownBy(() -> handler.handle(principal, payload)).isInstanceOf(AvatarTakenException.class);
        verify(playerService, never()).updateAvatar(any(Player.class), anyString());
        verifyNoInteractions(notifier);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"avatar\":\"unicorn\"}", "{\"avatar\":7}"})
    void shouldThrowInvalidAvatarExceptionWhenAvatarIsNotInTheCatalog(final String json) {
        final JsonNode payload = JSON_MAPPER.readTree(json);

        assertThatThrownBy(() -> handler.handle(principal, payload)).isInstanceOf(InvalidAvatarException.class);
        verifyNoInteractions(roomService, playerService, notifier);
    }

    @Test
    void shouldThrowInvalidAvatarExceptionWhenPayloadIsNull() {
        assertThatThrownBy(() -> handler.handle(principal, null)).isInstanceOf(InvalidAvatarException.class);
        verifyNoInteractions(roomService, playerService, notifier);
    }

    @ParameterizedTest
    @EnumSource(value = RoomStatus.class, names = "WAITING", mode = EnumSource.Mode.EXCLUDE)
    void shouldThrowGameAlreadyStartedExceptionWhenRoomIsNotWaiting(final RoomStatus status) throws BaseException {
        room.setStatus(status);
        when(roomService.getOrException(room.getCode())).thenReturn(room);
        final JsonNode payload = payload("robot");

        assertThatThrownBy(() -> handler.handle(principal, payload)).isInstanceOf(GameAlreadyStartedException.class);
        verifyNoInteractions(playerService, notifier);
    }
}
