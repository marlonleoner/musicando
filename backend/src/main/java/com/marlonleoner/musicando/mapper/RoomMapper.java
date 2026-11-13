package com.marlonleoner.musicando.mapper;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.dto.response.RoomCreatedResponse;
import com.marlonleoner.musicando.dto.response.RoomResponse;
import com.marlonleoner.musicando.dto.response.RoomStateBroadcast;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(uses = PlayerMapper.class)
public interface RoomMapper {

    RoomResponse toResponse(Room room);

    List<RoomResponse> toResponse(List<Room> rooms);

    RoomCreatedResponse toCreatedResponse(Room room);

    RoomStateBroadcast toStateBroadcast(Room room);
}
