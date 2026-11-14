package com.marlonleoner.musicando.handler.socket.command;

import com.marlonleoner.musicando.config.properties.RoomProperties;
import com.marlonleoner.musicando.domain.Match;
import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.GameAlreadyStartedException;
import com.marlonleoner.musicando.domain.exception.InvalidSettingsException;
import com.marlonleoner.musicando.domain.exception.NotEnoughPlayersException;
import com.marlonleoner.musicando.domain.music.GameRound;
import com.marlonleoner.musicando.domain.music.GameState;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.service.game.GameEngine;
import com.marlonleoner.musicando.service.game.RoundGenerator;
import com.marlonleoner.musicando.service.match.MatchService;
import com.marlonleoner.musicando.service.music.MusicService;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * {@code START_GAME} (somente VIP): valida a configuração, sorteia as rodadas a partir da playlist e coloca a sala em
 * andamento. Todos recebem o {@code ROOM_STATE} com o motivo {@code GAME_STARTED} e, em seguida, a contagem da primeira
 * rodada.
 */
@Component
@RequiredArgsConstructor
public class StartGameCommandHandler implements SocketCommandHandler {

    private final PlayerService playerService;

    private final RoomService roomService;

    private final MatchService matchService;

    private final MusicService musicService;

    private final RoundGenerator roundGenerator;

    private final GameEngine gameEngine;

    private final RoomStateNotifier notifier;

    private final RoomProperties roomProperties;

    @Override
    public void handle(SocketPrincipal principal, JsonNode payload) throws BaseException {
        playerService.getOwnerOrException(principal.participantId());
        final Room room = roomService.getOrException(principal.roomCode());

        if (!room.isWaiting()) {
            throw new GameAlreadyStartedException(room.getCode());
        }

        final Match match = room.getCurrentMatch();

        if (Objects.isNull(match) || !match.hasPlaylist()) {
            throw new InvalidSettingsException("Choose a playlist before starting the game");
        }

        final long connectedPlayers = Objects.requireNonNullElse(room.getPlayers(), List.<Player>of()).stream().filter(Player::isConnected).count();

        if (connectedPlayers < roomProperties.minPlayers()) {
            throw new NotEnoughPlayersException(roomProperties.minPlayers());
        }

        final List<GameRound> rounds = roundGenerator.generate(musicService.playlistTracks(match.getPlaylistId()), match.getRounds());
        final GameState state = GameState.builder()
                .matchId(match.getId())
                .roundDuration(match.getRoundDuration())
                .rounds(new ArrayList<>(rounds))
                .build();
        matchService.start(match);
        roomService.start(room);
        notifier.broadcast(principal.roomCode(), RoomStateReason.GAME_STARTED, principal.participantId());
        gameEngine.start(principal.roomCode(), state);
    }
}
