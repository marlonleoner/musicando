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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

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
}
