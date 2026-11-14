package com.marlonleoner.musicando.service.player;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    private static final String CODE = "ABC123";

    @Mock
    private PlayerRepository repository;

    @InjectMocks
    private PlayerService service;

    @Test
    void shouldReturnPlayerWhenFindingById() {
        final Player player = Fixture.make(Player.class);
        when(repository.findById(player.getId())).thenReturn(Optional.of(player));

        assertThat(service.findById(player.getId())).contains(player);
    }

    @Test
    void shouldCreatePlayerInRoomWhenCreatingPlayer() {
        final Room room = Fixture.make(Room.class);
        when(repository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final Player player = service.createAndSave("Marlon", room);

        assertThat(player.getNickname()).isEqualTo("Marlon");
        assertThat(player.getRoom()).isEqualTo(room);
        assertThat(player.getId()).isNotBlank();
        assertThat(player.getSecret()).isNotBlank().isNotEqualTo(player.getId());
        assertThat(player.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldReturnPlayerWhenGettingExistingPlayer() throws ObjectNotFoundException {
        final Player player = Fixture.make(Player.class);
        when(repository.findById(player.getId())).thenReturn(Optional.of(player));

        assertThat(service.getOrException(player.getId())).isEqualTo(player);
    }

    @Test
    void shouldThrowObjectNotFoundExceptionWhenPlayerDoesNotExist() {
        when(repository.findById("player-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOrException("player-id")).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    void shouldMarkPlayerAsConnectedWhenPlayerConnects() {
        final Player player = Fixture.make(Player.class);
        player.setConnected(false);

        service.onConnect(player, "session-1");

        verify(repository).save(player);
        assertThat(player.isConnected()).isTrue();
        assertThat(player.getSessionId()).isEqualTo("session-1");
        assertThat(player.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldMarkPlayerAsDisconnectedWhenPlayerDisconnects() {
        final Player player = Fixture.make(Player.class);
        player.setConnected(true);

        service.onDisconnect(player);

        verify(repository).save(player);
        assertThat(player.isConnected()).isFalse();
        assertThat(player.getSessionId()).isNull();
    }

    @Test
    void shouldReturnUpdatedPlayersWhenResettingConnections() {
        when(repository.resetConnections()).thenReturn(5);

        assertThat(service.resetConnections()).isEqualTo(5);
    }

    @Test
    void shouldDeletePlayerWhenDeleting() {
        final Player player = Fixture.make(Player.class);

        service.delete(player);

        verify(repository).delete(player);
    }

    @Test
    void shouldKeepOwnerWhenOwnerIsConnected() {
        final List<Player> players = players(true, true, false, true);
        when(repository.findByRoomCodeOrderByCreatedAtAsc(CODE)).thenReturn(players);

        assertThat(service.ensureOwner(CODE)).isFalse();
        verify(repository, never()).save(any());
    }

    @Test
    void shouldTransferOwnershipToOldestConnectedPlayerWhenOwnerIsDisconnected() {
        final List<Player> players = players(true, false, false, false, false, true, false, true);
        when(repository.findByRoomCodeOrderByCreatedAtAsc(CODE)).thenReturn(players);

        assertThat(service.ensureOwner(CODE)).isTrue();
        assertThat(players).extracting(Player::isOwner).containsExactly(false, false, true, false);
    }

    @Test
    void shouldKeepDisconnectedOwnerWhenNobodyIsConnected() {
        final List<Player> players = players(true, false, false, false);
        when(repository.findByRoomCodeOrderByCreatedAtAsc(CODE)).thenReturn(players);

        assertThat(service.ensureOwner(CODE)).isFalse();
        assertThat(players.getFirst().isOwner()).isTrue();
    }

    @Test
    void shouldMakeOldestPlayerOwnerWhenRoomHasNoOwnerAndNobodyIsConnected() {
        final List<Player> players = players(false, false, false, false);
        when(repository.findByRoomCodeOrderByCreatedAtAsc(CODE)).thenReturn(players);

        assertThat(service.ensureOwner(CODE)).isTrue();
        assertThat(players).extracting(Player::isOwner).containsExactly(true, false);
    }

    @Test
    void shouldMakeConnectedPlayerOwnerWhenOwnerLeftTheRoom() {
        final List<Player> players = players(false, false, false, true);
        when(repository.findByRoomCodeOrderByCreatedAtAsc(CODE)).thenReturn(players);

        assertThat(service.ensureOwner(CODE)).isTrue();
        assertThat(players).extracting(Player::isOwner).containsExactly(false, true);
    }

    @Test
    void shouldDoNothingWhenRoomHasNoPlayers() {
        when(repository.findByRoomCodeOrderByCreatedAtAsc(CODE)).thenReturn(List.of());

        assertThat(service.ensureOwner(CODE)).isFalse();
        verify(repository, never()).save(any());
    }

    /**
     * Cria jogadores em ordem de entrada a partir de pares (owner, connected).
     */
    private List<Player> players(final boolean... ownerAndConnected) {
        final List<Player> players = new ArrayList<>();
        for (int i = 0; i < ownerAndConnected.length; i += 2) {
            final Player player = Fixture.make(Player.class);
            player.setOwner(ownerAndConnected[i]);
            player.setConnected(ownerAndConnected[i + 1]);
            players.add(player);
        }

        return players;
    }
}
