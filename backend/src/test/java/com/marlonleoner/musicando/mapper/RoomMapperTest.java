package com.marlonleoner.musicando.mapper;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.dto.response.PlayerResponse;
import com.marlonleoner.musicando.dto.response.RoomCreatedResponse;
import com.marlonleoner.musicando.dto.response.RoomResponse;
import com.marlonleoner.musicando.dto.response.RoomStateBroadcast;
import com.marlonleoner.musicando.fixture.Fixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoomMapperTest {

    private RoomMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RoomMapperImpl();
        ReflectionTestUtils.setField(mapper, "playerMapper", new PlayerMapperImpl());
    }

    @Test
    void shouldMapPublicDataWhenMappingRoomToResponse() {
        final Room room = Fixture.make(Room.class);

        final RoomResponse response = mapper.toResponse(room);

        assertThat(response.code()).isEqualTo(room.getCode());
        assertThat(response.status()).isEqualTo(room.getStatus());
        assertThat(response.connected()).isEqualTo(room.isConnected());
    }

    @Test
    void shouldMapEveryRoomWhenMappingList() {
        final List<Room> rooms = Fixture.makeList(Room.class, 3);

        final List<RoomResponse> responses = mapper.toResponse(rooms);

        assertThat(responses).extracting(RoomResponse::code)
                .containsExactlyElementsOf(rooms.stream().map(Room::getCode).toList());
    }

    @Test
    void shouldMapIdCodeAndSecretWhenMappingRoomToCreatedResponse() {
        final Room room = Fixture.make(Room.class);

        final RoomCreatedResponse response = mapper.toCreatedResponse(room);

        assertThat(response.id()).isEqualTo(room.getId());
        assertThat(response.code()).isEqualTo(room.getCode());
        assertThat(response.secret()).isEqualTo(room.getSecret());
    }

    @Test
    void shouldMapPlayersWithoutSecretsWhenMappingRoomToStateBroadcast() {
        final Room room = Fixture.make(Room.class);
        room.setPlayers(Fixture.makeList(Player.class, 2));

        final RoomStateBroadcast broadcast = mapper.toStateBroadcast(room);

        assertThat(broadcast.code()).isEqualTo(room.getCode());
        assertThat(broadcast.status()).isEqualTo(room.getStatus());
        assertThat(broadcast.connected()).isEqualTo(room.isConnected());
        assertThat(broadcast.players()).extracting(PlayerResponse::id)
                .containsExactlyElementsOf(room.getPlayers().stream().map(Player::getId).toList());
    }

    @Test
    void shouldReturnEmptyPlayersWhenRoomHasNoPlayers() {
        final Room room = Fixture.make(Room.class);
        room.setPlayers(null);

        assertThat(mapper.toStateBroadcast(room).players()).isEmpty();
    }

    @Test
    void shouldReturnNullWhenRoomIsNull() {
        assertThat(mapper.toResponse((Room) null)).isNull();
        assertThat(mapper.toResponse((List<Room>) null)).isNull();
        assertThat(mapper.toCreatedResponse(null)).isNull();
        assertThat(mapper.toStateBroadcast(null)).isNull();
    }
}
