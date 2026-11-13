package com.marlonleoner.musicando.service.socket;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.ErrorCode;
import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.exception.SocketAuthenticationException;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocketAuthenticationServiceTest {

    private static final String CODE = "ABC123";

    private static final SocketPrincipal HOST = new SocketPrincipal(RoleEnum.HOST, "room-id", CODE);

    private static final SocketPrincipal PLAYER = new SocketPrincipal(RoleEnum.PLAYER, "player-id", CODE);

    @Mock
    private RoomService roomService;

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private SocketAuthenticationService service;

    @Test
    void shouldReturnHostPrincipalWhenHostCredentialsAreValid() {
        final Room room = Fixture.make(Room.class);
        when(roomService.findByCode(room.getCode())).thenReturn(Optional.of(room));

        final SocketPrincipal principal = service.authenticate("host", room.getCode(), room.getId(), room.getSecret());

        assertThat(principal).isEqualTo(new SocketPrincipal(RoleEnum.HOST, room.getId(), room.getCode()));
    }

    @ParameterizedTest
    @CsvSource({"wrong-id, VALID", "VALID, wrong-secret"})
    void shouldRejectHostWhenIdOrSecretDoesNotMatch(final String id, final String secret) {
        final Room room = Fixture.make(Room.class);
        when(roomService.findByCode(room.getCode())).thenReturn(Optional.of(room));
        final String providedId = "VALID".equals(id) ? room.getId() : id;
        final String providedSecret = "VALID".equals(secret) ? room.getSecret() : secret;

        assertInvalidCredentials(() -> service.authenticate("host", room.getCode(), providedId, providedSecret));
    }

    @Test
    void shouldRejectHostWhenRoomDoesNotExist() {
        when(roomService.findByCode(CODE)).thenReturn(Optional.empty());

        assertInvalidCredentials(() -> service.authenticate("host", CODE, "room-id", "room-secret"));
    }

    @Test
    void shouldReturnPlayerPrincipalWhenPlayerCredentialsAreValid() {
        final Player player = Fixture.make(Player.class);
        when(playerService.findById(player.getId())).thenReturn(Optional.of(player));

        final SocketPrincipal principal = service.authenticate("player", player.getRoom().getCode(), player.getId(), player.getSecret());

        assertThat(principal).isEqualTo(new SocketPrincipal(RoleEnum.PLAYER, player.getId(), player.getRoom().getCode()));
    }

    @Test
    void shouldRejectPlayerWhenPlayerBelongsToAnotherRoom() {
        final Player player = Fixture.make(Player.class);
        when(playerService.findById(player.getId())).thenReturn(Optional.of(player));

        assertInvalidCredentials(() -> service.authenticate("player", "OTHER1", player.getId(), player.getSecret()));
    }

    @Test
    void shouldRejectPlayerWhenSecretDoesNotMatch() {
        final Player player = Fixture.make(Player.class);
        when(playerService.findById(player.getId())).thenReturn(Optional.of(player));

        assertInvalidCredentials(() -> service.authenticate("player", player.getRoom().getCode(), player.getId(), "wrong-secret"));
    }

    @Test
    void shouldRejectPlayerWhenPlayerDoesNotExist() {
        when(playerService.findById("player-id")).thenReturn(Optional.empty());

        assertInvalidCredentials(() -> service.authenticate("player", CODE, "player-id", "secret"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin", "HOST", ""})
    void shouldRejectConnectionWhenRoleIsInvalid(final String role) {
        assertInvalidCredentials(() -> service.authenticate(role, CODE, "id", "secret"));
    }

    @Test
    void shouldRejectConnectionWhenAnyHeaderIsMissing() {
        assertInvalidCredentials(() -> service.authenticate(null, CODE, "id", "secret"));
        assertInvalidCredentials(() -> service.authenticate("host", null, "id", "secret"));
        assertInvalidCredentials(() -> service.authenticate("host", CODE, null, "secret"));
        assertInvalidCredentials(() -> service.authenticate("host", CODE, "id", null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/topic/room/ABC123", "/user/queue/events", "/user/queue/errors", "/musicando/room"})
    void shouldAllowPlayerSubscriptionWhenDestinationBelongsToParticipant(final String destination) {
        assertThatCode(() -> service.authorizeSubscription(PLAYER, destination)).doesNotThrowAnyException();
    }

    @Test
    void shouldAllowHostSubscriptionWhenDestinationIsHostTopic() {
        assertThatCode(() -> service.authorizeSubscription(HOST, "/topic/room/ABC123/host")).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/topic/room/ABC123/host", "/topic/room/OTHER1", "/topic/room/OTHER1/host", "/queue/events", "/topic/room/"})
    void shouldRejectPlayerSubscriptionWhenDestinationIsNotAllowed(final String destination) {
        assertForbidden(() -> service.authorizeSubscription(PLAYER, destination));
    }

    @Test
    void shouldRejectSubscriptionWhenDestinationIsMissing() {
        assertForbidden(() -> service.authorizeSubscription(HOST, null));
    }

    @Test
    void shouldAllowSendWhenDestinationIsCommandEndpoint() {
        assertThatCode(() -> service.authorizeSend("/musicando/command")).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/musicando/join", "/topic/room/ABC123", "/queue/events"})
    void shouldRejectSendWhenDestinationIsNotCommandEndpoint(final String destination) {
        assertForbidden(() -> service.authorizeSend(destination));
    }

    private void assertInvalidCredentials(final org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
        assertThatThrownBy(call).isInstanceOf(SocketAuthenticationException.class)
                .extracting(ex -> ((SocketAuthenticationException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    private void assertForbidden(final org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
        assertThatThrownBy(call).isInstanceOf(SocketAuthenticationException.class)
                .extracting(ex -> ((SocketAuthenticationException) ex).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }
}
