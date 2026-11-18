package com.marlonleoner.musicando.service.game;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.enums.RoomStatus;
import com.marlonleoner.musicando.domain.exception.MusicProviderException;
import com.marlonleoner.musicando.domain.music.LobbyMusic;
import com.marlonleoner.musicando.domain.music.MusicTrack;
import com.marlonleoner.musicando.domain.socket.SocketEventType;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.dto.response.LobbyAudioBroadcast;
import com.marlonleoner.musicando.dto.socket.SocketEvent;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.service.cache.RedisService;
import com.marlonleoner.musicando.service.music.MusicService;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.SocketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LobbyMusicServiceTest {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private static final String CODE = "ABC123";

    private static final String KEY = "@MUSICANDO:LOBBY_MUSIC:ABC123";

    private static final SocketPrincipal HOST = new SocketPrincipal(RoleEnum.HOST, "room-id", CODE);

    @Mock
    private MusicService musicService;

    @Mock
    private RoomService roomService;

    @Mock
    private SocketService socketService;

    @Mock
    private RedisService redisService;

    private LobbyMusicService service;

    @BeforeEach
    void setUp() {
        service = new LobbyMusicService(musicService, roomService, socketService, redisService, JSON_MAPPER, new Random(42));
    }

    private static List<MusicTrack> tracks(final int playable, final int unplayable) {
        return Stream.concat(
                IntStream.rangeClosed(1, playable).mapToObj(i -> new MusicTrack(String.valueOf(i), "Song " + i, "Artist", true)),
                IntStream.rangeClosed(1, unplayable).mapToObj(i -> new MusicTrack("x" + i, "Other " + i, "Artist", false))).toList();
    }

    private void givenPreviews() throws MusicProviderException {
        when(musicService.findPreview(anyString())).thenAnswer(invocation -> Optional.of("preview-" + invocation.getArgument(0) + ".mp3"));
    }

    private void givenStored(final LobbyMusic music) {
        when(redisService.get(KEY, String.class)).thenReturn(Optional.of(JSON_MAPPER.writeValueAsString(music)));
    }

    private void givenWaitingRoom(final String playlistId) {
        final Room room = Fixture.make(Room.class);
        room.setStatus(RoomStatus.WAITING);
        room.getCurrentMatch().setPlaylistId(playlistId);
        when(roomService.findByCode(CODE)).thenReturn(Optional.of(room));
    }

    private LobbyMusic savedMusic() {
        final ArgumentCaptor<Object> json = ArgumentCaptor.forClass(Object.class);
        verify(redisService).set(eq(KEY), json.capture(), eq(6 * 60 * 60));

        return JSON_MAPPER.readValue((String) json.getValue(), LobbyMusic.class);
    }

    private static List<String> previewsOf(final SocketEvent event) {
        assertThat(event.type()).isEqualTo(SocketEventType.LOBBY_AUDIO);

        return ((LobbyAudioBroadcast) event.payload()).previewUrls();
    }

    @Test
    void shouldDrawPlayableTracksSaveThemAndSendPreviewsToHostWhenRefreshing() throws MusicProviderException {
        when(musicService.playlistTracks("123")).thenReturn(tracks(20, 5));
        givenPreviews();

        service.refresh(CODE, "123");

        final LobbyMusic saved = savedMusic();
        assertThat(saved.playlistId()).isEqualTo("123");
        assertThat(saved.trackIds()).hasSize(LobbyMusicService.TRACKS).doesNotHaveDuplicates().noneMatch(id -> id.startsWith("x"));
        final ArgumentCaptor<SocketEvent> event = ArgumentCaptor.forClass(SocketEvent.class);
        verify(socketService).sendToHost(eq(CODE), event.capture());
        assertThat(previewsOf(event.getValue())).containsExactlyElementsOf(saved.trackIds().stream().map(id -> "preview-" + id + ".mp3").toList());
    }

    @Test
    void shouldDrawEveryPlayableTrackWhenPlaylistIsSmall() throws MusicProviderException {
        when(musicService.playlistTracks("123")).thenReturn(tracks(3, 2));
        givenPreviews();

        service.refresh(CODE, "123");

        assertThat(savedMusic().trackIds()).containsExactlyInAnyOrder("1", "2", "3");
    }

    @Test
    void shouldSkipTracksWithoutPreviewWhenSendingLobbyMusic() throws MusicProviderException {
        when(musicService.playlistTracks("123")).thenReturn(tracks(3, 0));
        when(musicService.findPreview("1")).thenReturn(Optional.of("preview-1.mp3"));
        when(musicService.findPreview("2")).thenReturn(Optional.empty());
        when(musicService.findPreview("3")).thenThrow(new MusicProviderException("unavailable"));

        service.refresh(CODE, "123");

        final ArgumentCaptor<SocketEvent> event = ArgumentCaptor.forClass(SocketEvent.class);
        verify(socketService).sendToHost(eq(CODE), event.capture());
        assertThat(previewsOf(event.getValue())).containsExactly("preview-1.mp3");
    }

    @Test
    void shouldClearStoredMusicAndSendNothingWhenProviderFailsToListTracks() throws MusicProviderException {
        when(musicService.playlistTracks("123")).thenThrow(new MusicProviderException("unavailable"));

        service.refresh(CODE, "123");

        verify(redisService).invalidate(KEY);
        verify(redisService, never()).set(anyString(), any(), any());
        verifyNoInteractions(socketService);
    }

    @Test
    void shouldIgnoreSendCurrentWhenParticipantIsPlayer() {
        service.sendCurrent(new SocketPrincipal(RoleEnum.PLAYER, "player-1", CODE));

        verifyNoInteractions(roomService, redisService, musicService, socketService);
    }

    @ParameterizedTest
    @EnumSource(value = RoomStatus.class, names = "WAITING", mode = EnumSource.Mode.EXCLUDE)
    void shouldSendNothingWhenRoomIsNotWaiting(final RoomStatus status) {
        final Room room = Fixture.make(Room.class);
        room.setStatus(status);
        when(roomService.findByCode(CODE)).thenReturn(Optional.of(room));

        service.sendCurrent(HOST);

        verifyNoInteractions(redisService, musicService, socketService);
    }

    @Test
    void shouldSendNothingWhenNoPlaylistWasChosen() {
        givenWaitingRoom(null);

        service.sendCurrent(HOST);

        verifyNoInteractions(redisService, musicService, socketService);
    }

    @Test
    void shouldSendNothingWhenRoomDoesNotExist() {
        when(roomService.findByCode(CODE)).thenReturn(Optional.empty());

        service.sendCurrent(HOST);

        verifyNoInteractions(redisService, musicService, socketService);
    }

    @Test
    void shouldSendStoredMusicWithFreshPreviewsToHostWhenItIsFromCurrentPlaylist() throws MusicProviderException {
        givenWaitingRoom("123");
        givenStored(new LobbyMusic("123", List.of("7", "3")));
        givenPreviews();

        service.sendCurrent(HOST);

        final ArgumentCaptor<SocketEvent> event = ArgumentCaptor.forClass(SocketEvent.class);
        verify(socketService).sendToParticipant(eq("room-id"), event.capture());
        assertThat(previewsOf(event.getValue())).containsExactly("preview-7.mp3", "preview-3.mp3");
        verify(musicService, never()).playlistTracks(anyString());
        verify(redisService, never()).set(anyString(), any(), any());
    }

    @Test
    void shouldDrawNewMusicWhenStoredMusicIsFromAnotherPlaylist() throws MusicProviderException {
        givenWaitingRoom("123");
        givenStored(new LobbyMusic("999", List.of("7")));
        when(musicService.playlistTracks("123")).thenReturn(tracks(2, 0));
        givenPreviews();

        service.sendCurrent(HOST);

        final LobbyMusic saved = savedMusic();
        assertThat(saved.playlistId()).isEqualTo("123");
        assertThat(saved.trackIds()).containsExactlyInAnyOrder("1", "2");
        verify(socketService).sendToParticipant(eq("room-id"), any(SocketEvent.class));
    }

    @Test
    void shouldDrawNewMusicWhenNothingIsStored() throws MusicProviderException {
        givenWaitingRoom("123");
        when(redisService.get(KEY, String.class)).thenReturn(Optional.empty());
        when(musicService.playlistTracks("123")).thenReturn(tracks(1, 0));
        givenPreviews();

        service.sendCurrent(HOST);

        assertThat(savedMusic().trackIds()).containsExactly("1");
        verify(socketService).sendToParticipant(eq("room-id"), any(SocketEvent.class));
    }

    @Test
    void shouldReturnStoredTrackIdsWhenLobbyHadMusic() {
        givenStored(new LobbyMusic("123", List.of("1", "2")));

        assertThat(service.trackIds(CODE)).containsExactlyInAnyOrder("1", "2");
    }

    @Test
    void shouldReturnNoTrackIdsWhenLobbyHadNoMusic() {
        when(redisService.get(KEY, String.class)).thenReturn(Optional.empty());

        assertThat(service.trackIds(CODE)).isEmpty();
    }

    @Test
    void shouldInvalidateStoredMusicWhenClearing() {
        service.clear(CODE);

        verify(redisService).invalidate(KEY);
    }
}
