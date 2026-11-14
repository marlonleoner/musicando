package com.marlonleoner.musicando.handler.socket.command;

import com.marlonleoner.musicando.domain.Match;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.enums.RoomStatus;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.GameAlreadyStartedException;
import com.marlonleoner.musicando.domain.exception.InvalidSettingsException;
import com.marlonleoner.musicando.domain.exception.NotRoomOwnerException;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.domain.exception.PlaylistNotFoundException;
import com.marlonleoner.musicando.domain.music.MusicPlaylist;
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
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateSettingsCommandHandlerTest {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    @Mock
    private PlayerService playerService;

    @Mock
    private RoomService roomService;

    @Mock
    private MatchService matchService;

    @Mock
    private MusicService musicService;

    @Mock
    private RoomStateNotifier notifier;

    @InjectMocks
    private UpdateSettingsCommandHandler handler;

    private Room room;

    private SocketPrincipal principal;

    @BeforeEach
    void setUp() {
        room = Fixture.make(Room.class);
        room.setStatus(RoomStatus.WAITING);
        principal = new SocketPrincipal(RoleEnum.PLAYER, "player-1", room.getCode());
    }

    private void givenRoom() throws BaseException {
        when(roomService.getOrException(room.getCode())).thenReturn(room);
    }

    @Test
    void shouldUpdateAllSettingsAndNotifyWhenPayloadHasEveryField() throws BaseException {
        givenRoom();
        final MusicPlaylist playlist = new MusicPlaylist("123", "Hits", "cover.jpg", 50);
        when(musicService.getPlaylistOrException("123")).thenReturn(playlist);

        handler.handle(principal, JSON_MAPPER.readTree("{\"playlistId\":\"123\",\"rounds\":15,\"roundDuration\":30}"));

        final Match match = room.getCurrentMatch();
        verify(matchService).updateRounds(match, 15);
        verify(matchService).updateRoundDuration(match, 30);
        verify(matchService).updatePlaylist(match, playlist);
        verify(notifier).broadcast(room.getCode(), RoomStateReason.SETTINGS_UPDATED, "player-1");
    }

    @Test
    void shouldUpdateOnlyRoundsWhenPayloadHasOnlyRounds() throws BaseException {
        givenRoom();

        handler.handle(principal, JSON_MAPPER.readTree("{\"rounds\":5,\"playlistId\":null}"));

        verify(matchService).updateRounds(room.getCurrentMatch(), 5);
        verify(matchService, never()).updateRoundDuration(eq(room.getCurrentMatch()), anyInt());
        verifyNoInteractions(musicService);
        verify(notifier).broadcast(room.getCode(), RoomStateReason.SETTINGS_UPDATED, "player-1");
    }

    @Test
    void shouldOnlyNotifyWhenPayloadIsNull() throws BaseException {
        givenRoom();

        handler.handle(principal, null);

        verifyNoInteractions(matchService, musicService);
        verify(notifier).broadcast(room.getCode(), RoomStateReason.SETTINGS_UPDATED, "player-1");
    }

    @Test
    void shouldCreateMatchWhenRoomHasNoCurrentMatch() throws BaseException {
        givenRoom();
        room.setCurrentMatch(null);
        final Match match = Fixture.make(Match.class);
        when(matchService.createFor(room)).thenReturn(match);

        handler.handle(principal, JSON_MAPPER.readTree("{\"roundDuration\":10}"));

        verify(roomService).attachMatch(room, match);
        verify(matchService).updateRoundDuration(match, 10);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"rounds\":7}", "{\"roundDuration\":45}"})
    void shouldThrowInvalidSettingsExceptionWhenValueIsNotAnOption(final String json) throws BaseException {
        givenRoom();
        final JsonNode payload = JSON_MAPPER.readTree(json);

        assertThatThrownBy(() -> handler.handle(principal, payload)).isInstanceOf(InvalidSettingsException.class);
        verify(matchService, never()).updateRounds(eq(room.getCurrentMatch()), anyInt());
        verifyNoInteractions(notifier);
    }

    @Test
    void shouldPropagatePlaylistNotFoundExceptionWhenPlaylistDoesNotExist() throws BaseException {
        givenRoom();
        when(musicService.getPlaylistOrException("999")).thenThrow(new PlaylistNotFoundException("999"));
        final JsonNode payload = JSON_MAPPER.readTree("{\"playlistId\":\"999\"}");

        assertThatThrownBy(() -> handler.handle(principal, payload)).isInstanceOf(PlaylistNotFoundException.class);
        verifyNoInteractions(notifier);
    }

    @ParameterizedTest
    @EnumSource(value = RoomStatus.class, names = "WAITING", mode = EnumSource.Mode.EXCLUDE)
    void shouldThrowGameAlreadyStartedExceptionWhenRoomIsNotWaiting(final RoomStatus status) throws BaseException {
        givenRoom();
        room.setStatus(status);

        assertThatThrownBy(() -> handler.handle(principal, null)).isInstanceOf(GameAlreadyStartedException.class);
        verifyNoInteractions(matchService, notifier);
    }

    @Test
    void shouldThrowNotRoomOwnerExceptionWhenPlayerIsNotOwner() throws BaseException {
        when(playerService.getOwnerOrException("player-1")).thenThrow(new NotRoomOwnerException());

        assertThatThrownBy(() -> handler.handle(principal, null)).isInstanceOf(NotRoomOwnerException.class);
        verifyNoInteractions(roomService, matchService, notifier);
    }
}
