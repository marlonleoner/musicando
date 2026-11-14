package com.marlonleoner.musicando.handler.socket.command;

import com.marlonleoner.musicando.config.properties.RoomProperties;
import com.marlonleoner.musicando.domain.Match;
import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.enums.RoomStatus;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.GameAlreadyStartedException;
import com.marlonleoner.musicando.domain.exception.InvalidSettingsException;
import com.marlonleoner.musicando.domain.exception.NotEnoughPlayersException;
import com.marlonleoner.musicando.domain.exception.NotRoomOwnerException;
import com.marlonleoner.musicando.domain.exception.PlaylistTooSmallException;
import com.marlonleoner.musicando.domain.music.GameRound;
import com.marlonleoner.musicando.domain.music.GameState;
import com.marlonleoner.musicando.domain.music.MusicTrack;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.service.game.GameStateService;
import com.marlonleoner.musicando.service.game.RoundGenerator;
import com.marlonleoner.musicando.service.match.MatchService;
import com.marlonleoner.musicando.service.music.MusicService;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartGameCommandHandlerTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private RoomService roomService;

    @Mock
    private MatchService matchService;

    @Mock
    private MusicService musicService;

    @Mock
    private RoundGenerator roundGenerator;

    @Mock
    private GameStateService gameStateService;

    @Mock
    private RoomStateNotifier notifier;

    private StartGameCommandHandler handler;

    private Room room;

    private Match match;

    private SocketPrincipal principal;

    @BeforeEach
    void setUp() {
        handler = new StartGameCommandHandler(playerService, roomService, matchService, musicService, roundGenerator, gameStateService, notifier,
                new RoomProperties(12, 2));
        room = Fixture.make(Room.class);
        room.setStatus(RoomStatus.WAITING);
        room.setPlayers(List.of(player(true), player(true), player(false)));
        match = room.getCurrentMatch();
        match.setPlaylistId("123");
        match.setRounds(10);
        match.setRoundDuration(20);
        principal = new SocketPrincipal(RoleEnum.PLAYER, "player-1", room.getCode());
    }

    private static Player player(final boolean connected) {
        final Player player = Fixture.make(Player.class);
        player.setConnected(connected);
        return player;
    }

    private void givenRoom() throws BaseException {
        when(roomService.getOrException(room.getCode())).thenReturn(room);
    }

    @Test
    void shouldGenerateRoundsSaveStateAndStartWhenSettingsAreValid() throws BaseException {
        givenRoom();
        final List<MusicTrack> tracks = List.of(new MusicTrack("1", "Song", "Artist", true));
        final List<GameRound> rounds = List.of(new GameRound("1", "SONG", List.of("SONG", "A", "B", "C")));
        when(musicService.playlistTracks("123")).thenReturn(tracks);
        when(roundGenerator.generate(tracks, 10)).thenReturn(rounds);

        handler.handle(principal, null);

        final InOrder order = inOrder(gameStateService, matchService, roomService, notifier);
        order.verify(gameStateService).save(room.getCode(), new GameState(match.getId(), 20, rounds, 0));
        order.verify(matchService).start(match);
        order.verify(roomService).start(room);
        order.verify(notifier).broadcast(room.getCode(), RoomStateReason.GAME_STARTED, "player-1");
    }

    @Test
    void shouldThrowInvalidSettingsExceptionWhenPlaylistWasNotChosen() throws BaseException {
        givenRoom();
        match.setPlaylistId(null);

        assertThatThrownBy(() -> handler.handle(principal, null)).isInstanceOf(InvalidSettingsException.class);
        verifyNoInteractions(musicService, gameStateService, notifier);
    }

    @Test
    void shouldThrowInvalidSettingsExceptionWhenRoomHasNoMatch() throws BaseException {
        givenRoom();
        room.setCurrentMatch(null);

        assertThatThrownBy(() -> handler.handle(principal, null)).isInstanceOf(InvalidSettingsException.class);
        verifyNoInteractions(musicService, gameStateService, notifier);
    }

    @Test
    void shouldThrowNotEnoughPlayersExceptionWhenConnectedPlayersAreBelowMinimum() throws BaseException {
        givenRoom();
        room.setPlayers(List.of(player(true), player(false)));

        assertThatThrownBy(() -> handler.handle(principal, null)).isInstanceOf(NotEnoughPlayersException.class);
        verifyNoInteractions(musicService, gameStateService, notifier);
    }

    @Test
    void shouldThrowNotEnoughPlayersExceptionWhenRoomHasNoPlayers() throws BaseException {
        givenRoom();
        room.setPlayers(null);

        assertThatThrownBy(() -> handler.handle(principal, null)).isInstanceOf(NotEnoughPlayersException.class);
    }

    @Test
    void shouldNotStartWhenPlaylistIsTooSmall() throws BaseException {
        givenRoom();
        when(musicService.playlistTracks("123")).thenReturn(List.of());
        when(roundGenerator.generate(List.of(), 10)).thenThrow(new PlaylistTooSmallException(10, 0));

        assertThatThrownBy(() -> handler.handle(principal, null)).isInstanceOf(PlaylistTooSmallException.class);
        verifyNoInteractions(gameStateService, matchService, notifier);
    }

    @ParameterizedTest
    @EnumSource(value = RoomStatus.class, names = "WAITING", mode = EnumSource.Mode.EXCLUDE)
    void shouldThrowGameAlreadyStartedExceptionWhenRoomIsNotWaiting(final RoomStatus status) throws BaseException {
        givenRoom();
        room.setStatus(status);

        assertThatThrownBy(() -> handler.handle(principal, null)).isInstanceOf(GameAlreadyStartedException.class);
        verifyNoInteractions(musicService, gameStateService, notifier);
    }

    @Test
    void shouldThrowNotRoomOwnerExceptionWhenPlayerIsNotOwner() throws BaseException {
        when(playerService.getOwnerOrException("player-1")).thenThrow(new NotRoomOwnerException());

        assertThatThrownBy(() -> handler.handle(principal, null)).isInstanceOf(NotRoomOwnerException.class);
        verifyNoInteractions(roomService, musicService, notifier);
    }
}
