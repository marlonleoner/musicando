package com.marlonleoner.musicando.mapper;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.dto.response.JoinRoomResponse;
import com.marlonleoner.musicando.dto.response.PlayerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface PlayerMapper {

    PlayerResponse toResponse(Player player);

    @Mapping(target = "playerId", source = "id")
    @Mapping(target = "roomCode", source = "room.code")
    JoinRoomResponse toJoinResponse(Player player);
}
