package com.marlonleoner.musicando.aggregation;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.RoomNotJoinableException;
import com.marlonleoner.musicando.dto.request.JoinRoomRequest;
import com.marlonleoner.musicando.dto.response.JoinRoomResponse;
import com.marlonleoner.musicando.dto.response.RoomCreatedResponse;
import com.marlonleoner.musicando.dto.response.RoomResponse;
import com.marlonleoner.musicando.mapper.PlayerMapper;
import com.marlonleoner.musicando.mapper.RoomMapper;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomAggregation {

    private final RoomService roomService;

    private final PlayerService playerService;

    private final RoomMapper roomMapper;

    private final PlayerMapper playerMapper;

    public List<RoomResponse> getAllRooms() {
        return roomMapper.toResponse(roomService.findAll());
    }

    public RoomCreatedResponse createRoom() {
        return roomMapper.toCreatedResponse(roomService.createAndSave());
    }

    /**
     * Permite que um jogador se junte a uma sala existente.
     *
     * @param params código da sala e apelido do jogador
     * @return identificador e segredo do jogador, além do código da sala
     * @throws BaseException            se a sala não existir
     * @throws RoomNotJoinableException se a sala não estiver aguardando jogadores (em jogo, pausada, finalizada ou
     *                                  fechada)
     */
    public JoinRoomResponse joinRoom(JoinRoomRequest params) throws BaseException {
        Room room = roomService.getOrException(params.code());

        if (!room.isWaiting()) {
            throw new RoomNotJoinableException(room.getCode(), room.getStatus());
        }

        Player player = playerService.createAndSave(params.nickname(), room);

        return playerMapper.toJoinResponse(player);
    }
}
