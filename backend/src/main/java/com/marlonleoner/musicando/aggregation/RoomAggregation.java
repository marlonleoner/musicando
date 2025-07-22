package com.marlonleoner.musicando.aggregation;

import java.util.List;

import org.springframework.stereotype.Service;

import com.marlonleoner.musicando.domain.dto.RoomDTO;
import com.marlonleoner.musicando.mapper.RoomMapper;
import com.marlonleoner.musicando.service.RoomService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomAggregation {

    private final RoomService roomService;

    public List<RoomDTO> getAllRooms() {
        return roomService.findAll().stream().map(RoomMapper::toDTO).toList();
    }

    public RoomDTO createRoom() {
        return RoomMapper.toDTO(roomService.create());
    }
}
