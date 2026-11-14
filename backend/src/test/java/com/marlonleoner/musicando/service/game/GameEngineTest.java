package com.marlonleoner.musicando.service.game;

import com.marlonleoner.musicando.config.properties.GameProperties;
import com.marlonleoner.musicando.domain.Match;
import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.ErrorCode;
import com.marlonleoner.musicando.domain.enums.GamePhase;
import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.enums.RoomStatus;
import com.marlonleoner.musicando.domain.exception.AnswerNotAcceptedException;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.MusicProviderException;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.domain.exception.SocketCommandException;
import com.marlonleoner.musicando.domain.music.GameRound;
import com.marlonleoner.musicando.domain.music.GameState;
import com.marlonleoner.musicando.domain.socket.RoomStateReason;
import com.marlonleoner.musicando.domain.socket.SocketEventType;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.dto.response.AnswerReceivedBroadcast;
import com.marlonleoner.musicando.dto.response.RoundAudioBroadcast;
import com.marlonleoner.musicando.dto.response.RoundCountdownBroadcast;
import com.marlonleoner.musicando.dto.response.RoundEndedBroadcast;
import com.marlonleoner.musicando.dto.response.RoundResultResponse;
import com.marlonleoner.musicando.dto.response.RoundStartedBroadcast;
import com.marlonleoner.musicando.dto.response.ScoreResponse;
import com.marlonleoner.musicando.dto.socket.SocketEvent;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.service.match.MatchService;
import com.marlonleoner.musicando.service.music.MusicService;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import com.marlonleoner.musicando.service.socket.SocketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GameEngineTest {

    private static final String CODE = "ABC123";

    private static final long START = 1_000_000L;

    private final MutableClock clock = new MutableClock(START);

    private final Map<String, GameState> storage = new HashMap<>();

    @Mock
    private GameTimer timer;

    @Mock
    private MusicService musicService;

    @Mock
    private RoomService roomService;

    @Mock
    private PlayerService playerService;

    @Mock
    private MatchService matchService;

    @Mock
    private SocketService socketService;

    @Mock
    private RoomStateNotifier notifier;

    private GameEngine engine;

    private Room room;

    private Player alice;

    private Player bruno;

    @BeforeEach
    void setUp() throws BaseException {
        final GameStateService gameStateService = new GameStateService(null, null) {
            @Override
            public void save(final String roomCode, final GameState state) {
                storage.put(roomCode, state);
            }

            @Override
            public Optional<GameState> find(final String roomCode) {
                return Optional.ofNullable(storage.get(roomCode));
            }

            @Override
            public void delete(final String roomCode) {
                storage.remove(roomCode);
            }
        };
        engine = new GameEngine(gameStateService, timer, new GameLocks(), musicService, roomService, playerService, matchService,
                socketService, notifier, new GameProperties(Duration.ofSeconds(3), Duration.ofSeconds(5), 5), clock);

        alice = player("alice", "ALICE", true);
        bruno = player("bruno", "BRUNO", true);
        room = Room.builder()
                .id("room-id")
                .code(CODE)
                .status(RoomStatus.IN_PROGRESS)
                .players(new ArrayList<>(List.of(alice, bruno, player("carla", "CARLA", false))))
                .currentMatch(Fixture.make(Match.class))
                .build();
        when(roomService.getOrException(CODE)).thenReturn(room);
        when(roomService.findByCode(CODE)).thenReturn(Optional.of(room));
        when(musicService.findPreview(anyString())).thenAnswer(invocation -> Optional.of("preview-" + invocation.getArgument(0) + ".mp3"));
    }

    private static Player player(final String id, final String nickname, final boolean connected) {
        return Player.builder().id(id).nickname(nickname).connected(connected).build();
    }

    private static GameState newState() {
        return GameState.builder()
                .matchId("match-id")
                .roundDuration(20)
                .rounds(new ArrayList<>(List.of(
                        new GameRound("t1", "SONG A", List.of("SONG B", "SONG A", "SONG C", "SONG D")),
                        new GameRound("t2", "SONG E", List.of("SONG E", "SONG F", "SONG G", "SONG H")))))
                .build();
    }

    private GameState state() {
        return storage.get(CODE);
    }

    /** Executa a última transição agendada, como se o tempo da etapa tivesse acabado. */
    private void fireTimer() {
        final ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
        verify(timer, atLeastOnce()).schedule(eq(CODE), anyLong(), task.capture());
        clock.set(state().getPhaseEndsAt());
        task.getValue().run();
    }

    private long lastDelay() {
        final ArgumentCaptor<Long> delay = ArgumentCaptor.forClass(Long.class);
        verify(timer, atLeastOnce()).schedule(eq(CODE), delay.capture(), any());

        return delay.getValue();
    }

    private List<SocketEvent> roomEvents(final SocketEventType type) {
        final ArgumentCaptor<SocketEvent> events = ArgumentCaptor.forClass(SocketEvent.class);
        verify(socketService, atLeastOnce()).broadcastToRoom(eq(CODE), events.capture());

        return events.getAllValues().stream().filter(event -> event.type() == type).toList();
    }

    @SuppressWarnings("unchecked")
    private <T> T lastRoomPayload(final SocketEventType type) {
        final List<SocketEvent> events = roomEvents(type);

        return (T) events.getLast().payload();
    }

    private RoundAudioBroadcast lastHostAudio() {
        final ArgumentCaptor<SocketEvent> events = ArgumentCaptor.forClass(SocketEvent.class);
        verify(socketService, atLeastOnce()).sendToHost(eq(CODE), events.capture());

        return (RoundAudioBroadcast) events.getValue().payload();
    }

    private void startPlaying() throws BaseException {
        engine.start(CODE, newState());
        fireTimer();
    }

    @Test
    void shouldStartCountdownOfFirstRoundAndSendAudioToHostWhenStarting() throws BaseException {
        engine.start(CODE, newState());

        assertThat(state().getPhase()).isEqualTo(GamePhase.COUNTDOWN);
        assertThat(state().getCurrentRound()).isZero();
        assertThat(state().getPreviewUrl()).isEqualTo("preview-t1.mp3");
        assertThat(lastDelay()).isEqualTo(3000);
        assertThat(this.<RoundCountdownBroadcast>lastRoomPayload(SocketEventType.ROUND_COUNTDOWN))
                .isEqualTo(new RoundCountdownBroadcast(1, 2, 3000));
        assertThat(lastHostAudio()).isEqualTo(new RoundAudioBroadcast(1, "preview-t1.mp3", 0));
    }

    @Test
    void shouldStartRoundWithoutPreviewWhenProviderFails() throws BaseException {
        when(musicService.findPreview("t1")).thenThrow(new MusicProviderException("down"));

        engine.start(CODE, newState());

        assertThat(state().getPreviewUrl()).isNull();
        assertThat(lastHostAudio().previewUrl()).isNull();
    }

    @Test
    void shouldPlayRoundAndSendOptionsWhenCountdownEnds() throws BaseException {
        startPlaying();

        assertThat(state().getPhase()).isEqualTo(GamePhase.PLAYING);
        assertThat(lastDelay()).isEqualTo(20000);
        assertThat(this.<RoundStartedBroadcast>lastRoomPayload(SocketEventType.ROUND_STARTED)).isEqualTo(
                new RoundStartedBroadcast(1, 2, List.of("SONG B", "SONG A", "SONG C", "SONG D"), 20000, 20000, List.of()));
    }

    @Test
    void shouldScoreCorrectAnswerByRemainingTimeAndNotifyWhenPlayerAnswers() throws BaseException {
        startPlaying();
        clock.advance(5000);

        engine.submitAnswer(CODE, "alice", 1, 1);

        assertThat(state().getAnswers().get("alice").points()).isEqualTo(875);
        assertThat(state().getAnswers().get("alice").correct()).isTrue();
        assertThat(state().getPhase()).isEqualTo(GamePhase.PLAYING);
        assertThat(this.<AnswerReceivedBroadcast>lastRoomPayload(SocketEventType.ANSWER_RECEIVED))
                .isEqualTo(new AnswerReceivedBroadcast(1, "alice", 1, 2));
    }

    @Test
    void shouldGiveZeroPointsWhenAnswerIsWrong() throws BaseException {
        startPlaying();

        engine.submitAnswer(CODE, "alice", 1, 0);

        assertThat(state().getAnswers().get("alice").correct()).isFalse();
        assertThat(state().getAnswers().get("alice").points()).isZero();
    }

    @Test
    void shouldEndRoundEarlyWithFastestAndScoreboardWhenAllConnectedPlayersAnswered() throws BaseException {
        startPlaying();
        clock.advance(4000);
        engine.submitAnswer(CODE, "bruno", 1, 1);
        clock.advance(4000);
        engine.submitAnswer(CODE, "alice", 1, 1);

        assertThat(state().getPhase()).isEqualTo(GamePhase.REVEAL);
        assertThat(state().getScores()).containsEntry("bruno", 900).containsEntry("alice", 800);
        assertThat(state().getRoundsWon()).containsEntry("bruno", 1).containsEntry("alice", 1);
        assertThat(lastDelay()).isEqualTo(5000);

        final RoundEndedBroadcast ended = lastRoomPayload(SocketEventType.ROUND_ENDED);
        assertThat(ended.answer()).isEqualTo("SONG A");
        assertThat(ended.lastRound()).isFalse();
        assertThat(ended.fastest()).extracting(RoundResultResponse::playerId).containsExactly("bruno", "alice");
        assertThat(ended.scoreboard()).containsExactly(new ScoreResponse("bruno", "BRUNO", 900), new ScoreResponse("alice", "ALICE", 800),
                new ScoreResponse("carla", "CARLA", 0));
        assertThat(ended.results()).contains(new RoundResultResponse("carla", "CARLA", false, false, 0, null));
    }

    @Test
    void shouldEndRoundWithUnansweredPlayersWhenTimeIsUp() throws BaseException {
        startPlaying();
        engine.submitAnswer(CODE, "alice", 1, 2);

        fireTimer();

        final RoundEndedBroadcast ended = lastRoomPayload(SocketEventType.ROUND_ENDED);
        assertThat(ended.fastest()).isEmpty();
        assertThat(ended.results()).contains(new RoundResultResponse("bruno", "BRUNO", false, false, 0, null));
        assertThat(state().getScores()).isEmpty();
    }

    @Test
    void shouldStartNextRoundCountdownWhenRevealEndsAndThereAreRoundsLeft() throws BaseException {
        startPlaying();
        engine.submitAnswer(CODE, "alice", 1, 1);
        fireTimer();

        fireTimer();

        assertThat(state().getPhase()).isEqualTo(GamePhase.COUNTDOWN);
        assertThat(state().getCurrentRound()).isEqualTo(1);
        assertThat(state().getAnswers()).isEmpty();
        assertThat(state().getScores()).containsKey("alice");
        assertThat(lastHostAudio()).isEqualTo(new RoundAudioBroadcast(2, "preview-t2.mp3", 0));
    }

    @Test
    void shouldFinishGameAndPersistResultsWhenLastRevealEnds() throws BaseException {
        startPlaying();
        engine.submitAnswer(CODE, "alice", 1, 1);
        fireTimer();
        fireTimer();
        fireTimer();
        fireTimer();

        assertThat(this.<RoundEndedBroadcast>lastRoomPayload(SocketEventType.ROUND_ENDED).lastRound()).isTrue();

        fireTimer();

        assertThat(state().getPhase()).isEqualTo(GamePhase.FINISHED);
        verify(playerService).applyResults(room.getPlayers(), state().getScores(), state().getRoundsWon());
        verify(matchService).finish(room.getCurrentMatch());
        verify(roomService).finish(room);
        verify(notifier).broadcast(CODE, RoomStateReason.GAME_FINISHED, null);
        verify(timer, atLeastOnce()).cancel(CODE);
    }

    @Test
    void shouldIgnoreStaleTimerWhenPhaseAlreadyChanged() throws BaseException {
        startPlaying();
        final ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
        verify(timer, atLeastOnce()).schedule(eq(CODE), anyLong(), task.capture());
        final Runnable playingTimer = task.getValue();
        engine.submitAnswer(CODE, "alice", 1, 1);
        engine.submitAnswer(CODE, "bruno", 1, 1);

        playingTimer.run();

        assertThat(state().getPhase()).isEqualTo(GamePhase.REVEAL);
        assertThat(roomEvents(SocketEventType.ROUND_ENDED)).hasSize(1);
    }

    @Test
    void shouldLogInsteadOfFailingWhenTransitionFails() throws BaseException {
        startPlaying();
        engine.submitAnswer(CODE, "alice", 1, 1);
        engine.submitAnswer(CODE, "bruno", 1, 1);
        fireTimer();
        fireTimer();
        when(roomService.getOrException(CODE)).thenThrow(new ObjectNotFoundException(Room.class));

        assertThatCode(this::fireTimer).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectAnswerWhenRoundIsNotPlaying() throws BaseException {
        engine.start(CODE, newState());

        assertThatThrownBy(() -> engine.submitAnswer(CODE, "alice", 1, 1)).isInstanceOf(AnswerNotAcceptedException.class);
    }

    @Test
    void shouldRejectAnswerWhenRoundNumberIsNotTheCurrentOne() throws BaseException {
        startPlaying();

        assertThatThrownBy(() -> engine.submitAnswer(CODE, "alice", 2, 1)).isInstanceOf(AnswerNotAcceptedException.class);
    }

    @Test
    void shouldRejectSecondAnswerWhenPlayerAlreadyAnswered() throws BaseException {
        startPlaying();
        engine.submitAnswer(CODE, "alice", 1, 0);

        assertThatThrownBy(() -> engine.submitAnswer(CODE, "alice", 1, 1)).isInstanceOf(AnswerNotAcceptedException.class);
        assertThat(state().getAnswers().get("alice").option()).isZero();
    }

    @Test
    void shouldRejectAnswerWhenGameIsPaused() throws BaseException {
        startPlaying();
        engine.pause(CODE);

        assertThatThrownBy(() -> engine.submitAnswer(CODE, "alice", 1, 1)).isInstanceOf(AnswerNotAcceptedException.class);
    }

    @Test
    void shouldRejectAnswerWhenThereIsNoGame() {
        assertThatThrownBy(() -> engine.submitAnswer(CODE, "alice", 1, 1)).isInstanceOf(AnswerNotAcceptedException.class);
    }

    @Test
    void shouldThrowInvalidCommandWhenOptionDoesNotExist() throws BaseException {
        startPlaying();

        assertThatThrownBy(() -> engine.submitAnswer(CODE, "alice", 1, 4))
                .isInstanceOf(SocketCommandException.class)
                .extracting(ex -> ((SocketCommandException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_COMMAND);
        assertThatThrownBy(() -> engine.submitAnswer(CODE, "alice", 1, -1)).isInstanceOf(SocketCommandException.class);
    }

    @Test
    void shouldFreezeRemainingTimeAndCancelTimerWhenPausing() throws BaseException {
        startPlaying();
        clock.advance(8000);

        engine.pause(CODE);
        engine.pause(CODE);

        assertThat(state().getPausedRemainingMs()).isEqualTo(12000);
        verify(timer, atLeastOnce()).cancel(CODE);
    }

    @Test
    void shouldResumeFromRemainingTimeAndResendPhaseWhenResuming() throws BaseException {
        startPlaying();
        clock.advance(8000);
        engine.pause(CODE);
        clock.advance(60000);

        engine.resume(CODE);

        assertThat(state().paused()).isFalse();
        assertThat(state().getPhaseEndsAt()).isEqualTo(clock.millis() + 12000);
        assertThat(state().getPhaseStartedAt()).isEqualTo(clock.millis() - 8000);
        assertThat(lastDelay()).isEqualTo(12000);
        assertThat(this.<RoundStartedBroadcast>lastRoomPayload(SocketEventType.ROUND_STARTED).durationMs()).isEqualTo(12000);
        assertThat(lastHostAudio()).isEqualTo(new RoundAudioBroadcast(1, "preview-t1.mp3", 8000));

        clock.advance(2000);
        engine.submitAnswer(CODE, "alice", 1, 1);
        assertThat(state().getAnswers().get("alice").responseTimeMs()).isEqualTo(10000);
    }

    @Test
    void shouldIgnorePauseAndResumeWhenThereIsNoRunningGame() {
        engine.pause(CODE);
        engine.resume(CODE);

        verifyNoInteractions(timer, socketService);
    }

    @Test
    void shouldIgnoreResumeWhenGameIsNotPaused() throws BaseException {
        startPlaying();

        engine.resume(CODE);

        assertThat(roomEvents(SocketEventType.ROUND_STARTED)).hasSize(1);
    }

    @Test
    void shouldSendCurrentPhaseAndAudioWithOffsetWhenHostReconnects() throws BaseException {
        startPlaying();
        clock.advance(6000);

        engine.sendCurrentPhase(new SocketPrincipal(RoleEnum.HOST, room.getId(), CODE));

        final ArgumentCaptor<SocketEvent> events = ArgumentCaptor.forClass(SocketEvent.class);
        verify(socketService, atLeastOnce()).sendToParticipant(eq(room.getId()), events.capture());
        assertThat(events.getAllValues()).extracting(SocketEvent::type).containsExactly(SocketEventType.ROUND_STARTED, SocketEventType.ROUND_AUDIO);
        assertThat(((RoundStartedBroadcast) events.getAllValues().getFirst().payload()).durationMs()).isEqualTo(14000);
        assertThat(((RoundAudioBroadcast) events.getAllValues().getLast().payload()).offsetMs()).isEqualTo(6000);
    }

    @Test
    void shouldSendOnlyCurrentPhaseWhenPlayerReconnects() throws BaseException {
        startPlaying();
        engine.submitAnswer(CODE, "alice", 1, 1);

        engine.sendCurrentPhase(new SocketPrincipal(RoleEnum.PLAYER, "alice", CODE));

        final ArgumentCaptor<SocketEvent> events = ArgumentCaptor.forClass(SocketEvent.class);
        verify(socketService).sendToParticipant(eq("alice"), events.capture());
        assertThat(((RoundStartedBroadcast) events.getValue().payload()).answeredPlayerIds()).containsExactly("alice");
    }

    @Test
    void shouldSendRevealWithoutAudioWhenHostReconnectsDuringReveal() throws BaseException {
        startPlaying();
        fireTimer();

        engine.sendCurrentPhase(new SocketPrincipal(RoleEnum.HOST, room.getId(), CODE));

        final ArgumentCaptor<SocketEvent> events = ArgumentCaptor.forClass(SocketEvent.class);
        verify(socketService).sendToParticipant(eq(room.getId()), events.capture());
        assertThat(events.getValue().type()).isEqualTo(SocketEventType.ROUND_ENDED);
    }

    @Test
    void shouldNotReadGameStateWhenRoomIsNotPlaying() throws BaseException {
        startPlaying();
        room.setStatus(RoomStatus.WAITING);

        engine.sendCurrentPhase(new SocketPrincipal(RoleEnum.HOST, room.getId(), CODE));
        when(roomService.findByCode(CODE)).thenReturn(Optional.empty());
        engine.sendCurrentPhase(new SocketPrincipal(RoleEnum.HOST, room.getId(), CODE));

        verify(socketService, never()).sendToParticipant(anyString(), any());
    }

    @Test
    void shouldSendNothingWhenThereIsNoRunningGame() {
        engine.sendCurrentPhase(new SocketPrincipal(RoleEnum.HOST, "room-id", CODE));
        storage.put(CODE, GameState.builder().phase(GamePhase.FINISHED).build());
        engine.sendCurrentPhase(new SocketPrincipal(RoleEnum.HOST, "room-id", CODE));

        verify(socketService, never()).sendToParticipant(anyString(), any());
    }

    @Test
    void shouldCancelTimerAndDiscardStateWhenStopping() throws BaseException {
        engine.start(CODE, newState());

        engine.stop(CODE);

        verify(timer).cancel(CODE);
        assertThat(state()).isNull();
    }

    private static final class MutableClock extends Clock {

        private long millis;

        private MutableClock(final long initial) {
            this.millis = initial;
        }

        void set(final long value) {
            millis = value;
        }

        void advance(final long delta) {
            millis += delta;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(final ZoneId zone) {
            return this;
        }

        @Override
        public long millis() {
            return millis;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis);
        }
    }
}
