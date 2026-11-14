package com.marlonleoner.musicando.mapper;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.dto.response.RoomCreatedResponse;
import com.marlonleoner.musicando.dto.response.RoomResponse;
import com.marlonleoner.musicando.dto.response.RoomStateBroadcast;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(uses = {PlayerMapper.class, MatchMapper.class})
public interface RoomMapper {

    RoomResponse toResponse(Room room);

    List<RoomResponse> toResponse(List<Room> rooms);

    RoomCreatedResponse toCreatedResponse(Room room);

    @Mapping(target = "settings", source = "currentMatch")
    RoomStateBroadcast toStateBroadcast(Room room);
}
