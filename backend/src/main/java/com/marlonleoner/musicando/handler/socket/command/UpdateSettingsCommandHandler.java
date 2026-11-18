package com.marlonleoner.musicando.handler.socket.command;

import com.marlonleoner.musicando.domain.Match;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.GameAlreadyStartedException;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.service.game.LobbyMusicService;
import com.marlonleoner.musicando.service.match.MatchService;
import com.marlonleoner.musicando.service.music.MusicService;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import com.marlonleoner.musicando.validator.MatchSettingsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.Objects;

/**
 * {@code UPDATE_SETTINGS} (somente VIP, com a sala aguardando jogadores). Payload com qualquer combinação dos campos:
 * {@code { "playlistId": "...", "rounds": 10, "roundDuration": 20 }}. Todos na sala recebem o {@code ROOM_STATE} com
 * a nova configuração. Ao trocar a playlist, a TV recebe também a nova música de fundo do lobby.
 */
@Component
@RequiredArgsConstructor
public class UpdateSettingsCommandHandler implements SocketCommandHandler {

    private static final String PLAYLIST_ID_FIELD = "playlistId";

    private static final String ROUNDS_FIELD = "rounds";

    private static final String ROUND_DURATION_FIELD = "roundDuration";

    private final PlayerService playerService;

    private final RoomService roomService;

    private final MatchService matchService;

    private final MusicService musicService;

    private final RoomStateNotifier notifier;

    private final LobbyMusicService lobbyMusicService;

    @Override
    public void handle(SocketPrincipal principal, JsonNode payload) throws BaseException {
        playerService.getOwnerOrException(principal.participantId());
        final Room room = roomService.getOrException(principal.roomCode());

        if (!room.isWaiting()) {
            throw new GameAlreadyStartedException(room.getCode());
        }

        final Match match = Objects.isNull(room.getCurrentMatch()) ? createMatch(room) : room.getCurrentMatch();

        if (hasField(payload, ROUNDS_FIELD)) {
            final int rounds = payload.get(ROUNDS_FIELD).asInt();
            MatchSettingsValidator.validateRounds(rounds);
            matchService.updateRounds(match, rounds);
        }

        if (hasField(payload, ROUND_DURATION_FIELD)) {
            final int roundDuration = payload.get(ROUND_DURATION_FIELD).asInt();
            MatchSettingsValidator.validateRoundDuration(roundDuration);
            matchService.updateRoundDuration(match, roundDuration);
        }

        if (hasField(payload, PLAYLIST_ID_FIELD)) {
            matchService.updatePlaylist(match, musicService.getPlaylistOrException(payload.get(PLAYLIST_ID_FIELD).asString()));
        }

        notifier.broadcast(principal.roomCode(), RoomStateReason.SETTINGS_UPDATED, principal.participantId());

        if (hasField(payload, PLAYLIST_ID_FIELD)) {
            lobbyMusicService.refresh(principal.roomCode(), match.getPlaylistId());
        }
    }

    private boolean hasField(JsonNode payload, String field) {
        return Objects.nonNull(payload) && payload.hasNonNull(field);
    }

    private Match createMatch(Room room) {
        final Match match = matchService.createFor(room);
        roomService.attachMatch(room, match);

        return match;
    }
}
