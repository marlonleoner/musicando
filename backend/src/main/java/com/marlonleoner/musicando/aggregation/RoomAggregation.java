package com.marlonleoner.musicando.aggregation;

import java.util.List;

import org.springframework.stereotype.Service;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.dto.JoinRoomDTO;
import com.marlonleoner.musicando.domain.dto.RoomDTO;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.RoomAlreadyFinishedException;
import com.marlonleoner.musicando.domain.request.JoinRoomRequest;
import com.marlonleoner.musicando.mapper.RoomMapper;
import com.marlonleoner.musicando.service.PlayerService;
import com.marlonleoner.musicando.service.RoomService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomAggregation {

    private final RoomService roomService;

    private final PlayerService playerService;

    public List<RoomDTO> getAllRooms() {
        return roomService.findAll().stream().map(RoomMapper::toDTO).toList();
    }

    public RoomDTO createRoom() {
        return RoomMapper.toDTO(roomService.createAndSave());
    }

    public JoinRoomDTO joinRoom(JoinRoomRequest params) throws BaseException {
        Room room = roomService.getOrException(params.code());

        if (room.isFinished()) {
            throw new RoomAlreadyFinishedException(room.getCode());
        }

        Player player = playerService.createAndSave(params.nickname(), room);

        return new JoinRoomDTO(player.getId(), player.getSecret(), room.getCode());
    }
}
