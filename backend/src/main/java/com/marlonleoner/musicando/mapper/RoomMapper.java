package com.marlonleoner.musicando.mapper;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.dto.RoomDTO;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RoomMapper {

    public static RoomDTO toDTO(Room room) {
        RoomDTO dto = new RoomDTO(room.getId().toString(), room.getCode());

        return dto;
    }
}