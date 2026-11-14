package com.marlonleoner.musicando.aggregation;

import com.marlonleoner.musicando.config.properties.RoomProperties;
import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.dto.request.JoinRoomRequest;
import com.marlonleoner.musicando.dto.response.JoinRoomResponse;
import com.marlonleoner.musicando.dto.response.RoomCreatedResponse;
import com.marlonleoner.musicando.dto.response.RoomResponse;
import com.marlonleoner.musicando.mapper.PlayerMapper;
import com.marlonleoner.musicando.mapper.RoomMapper;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import com.marlonleoner.musicando.validator.JoinRoomValidator;
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

    private final RoomStateNotifier notifier;

    private final RoomProperties roomProperties;

    public List<RoomResponse> getAllRooms() {
        return roomMapper.toResponse(roomService.findAll());
    }

    public RoomCreatedResponse createRoom() {
        return roomMapper.toCreatedResponse(roomService.createAndSave());
    }

    /**
     * Permite que um jogador entre em uma sala que está aguardando jogadores. O apelido é normalizado (caixa alta) e
     * precisa ser único na sala. O primeiro jogador a entrar se torna o VIP.
     *
     * @param params código da sala e apelido do jogador
     * @return identificador e segredo do jogador, além do código da sala
     * @throws BaseException se a sala não existir, não aceitar jogadores, estiver cheia ou se o apelido for inválido
     *                       ou já estiver em uso
     */
    public JoinRoomResponse joinRoom(JoinRoomRequest params) throws BaseException {
        final String nickname = JoinRoomValidator.normalizeNickname(params.nickname());
        final Room room = roomService.getOrException(params.code());
        JoinRoomValidator.validateRoom(room, nickname, roomProperties.maxPlayers());

        final Player player = playerService.createAndSave(nickname, room);
        playerService.ensureOwner(room.getCode());
        notifier.broadcast(room.getCode(), RoomStateReason.PLAYER_JOINED, player.getId());

        return playerMapper.toJoinResponse(player);
    }
}
