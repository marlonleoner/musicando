package com.marlonleoner.musicando.mapper;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.dto.response.JoinRoomResponse;
import com.marlonleoner.musicando.dto.response.PlayerResponse;
import com.marlonleoner.musicando.fixture.Fixture;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerMapperTest {

    private final PlayerMapper mapper = new PlayerMapperImpl();

    @Test
    void shouldMapPublicDataWhenMappingPlayerToResponse() {
        final Player player = Fixture.make(Player.class);

        final PlayerResponse response = mapper.toResponse(player);

        assertThat(response.id()).isEqualTo(player.getId());
        assertThat(response.nickname()).isEqualTo(player.getNickname());
        assertThat(response.score()).isEqualTo(player.getScore());
        assertThat(response.owner()).isEqualTo(player.isOwner());
        assertThat(response.connected()).isEqualTo(player.isConnected());
    }

    @Test
    void shouldMapIdSecretAndRoomCodeWhenMappingPlayerToJoinResponse() {
        final Player player = Fixture.make(Player.class);

        final JoinRoomResponse response = mapper.toJoinResponse(player);

        assertThat(response.playerId()).isEqualTo(player.getId());
        assertThat(response.secret()).isEqualTo(player.getSecret());
        assertThat(response.roomCode()).isEqualTo(player.getRoom().getCode());
    }

    @Test
    void shouldReturnNullWhenPlayerIsNull() {
        assertThat(mapper.toResponse(null)).isNull();
        assertThat(mapper.toJoinResponse(null)).isNull();
    }
}
