package com.marlonleoner.musicando.service.game;

import com.marlonleoner.musicando.config.properties.GameProperties;
import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.ErrorCode;
import com.marlonleoner.musicando.domain.enums.GamePhase;
import com.marlonleoner.musicando.domain.exception.AnswerNotAcceptedException;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.MusicProviderException;
import com.marlonleoner.musicando.domain.exception.SocketCommandException;
import com.marlonleoner.musicando.domain.music.GameRound;
import com.marlonleoner.musicando.domain.music.GameState;
import com.marlonleoner.musicando.domain.music.PlayerAnswer;
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
import com.marlonleoner.musicando.service.match.MatchService;
import com.marlonleoner.musicando.service.music.MusicService;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.RoomStateNotifier;
import com.marlonleoner.musicando.service.socket.SocketService;
import com.marlonleoner.musicando.util.LogSanitizer;
import com.marlonleoner.musicando.util.ScoreCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Ciclo das rodadas de uma partida, controlado pelo servidor:
 * {@code COUNTDOWN -> PLAYING -> REVEAL -> (próxima rodada | FINISHED)}. O estado fica no Redis e cada transição é
 * agendada no {@link GameTimer}. Toda alteração do estado acontece sob o lock da sala.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameEngine {

    private static final long MILLIS_PER_SECOND = 1000L;

    private final GameStateService gameStateService;

    private final GameTimer timer;

    private final GameLocks locks;

    private final MusicService musicService;

    private final RoomService roomService;

    private final PlayerService playerService;

    private final MatchService matchService;

    private final SocketService socketService;

    private final RoomStateNotifier notifier;

    private final GameProperties properties;

    private final Clock clock;

    /**
     * Começa a partida pela contagem da primeira rodada.
     */
    public void start(String roomCode, GameState state) throws BaseException {
        locks.run(roomCode, () -> startCountdown(roomCode, state, 0));
    }

    /**
     * Registra a resposta de um jogador. Quando todos os jogadores conectados já responderam, a rodada termina na hora.
     *
     * @param round  número da rodada (começa em 1)
     * @param option índice da opção escolhida
     */
    public void submitAnswer(String roomCode, String playerId, int round, int option) throws BaseException {
        locks.run(roomCode, () -> {
            final GameState state = gameStateService.find(roomCode)
                    .orElseThrow(() -> new AnswerNotAcceptedException("There is no game running in this room"));

            if (state.getPhase() != GamePhase.PLAYING || state.paused() || state.getCurrentRound() + 1 != round) {
                throw new AnswerNotAcceptedException("Answers for this round are not being accepted");
            }

            if (state.getAnswers().containsKey(playerId)) {
                throw new AnswerNotAcceptedException("You have already answered this round");
            }

            final GameRound current = state.round();

            if (option < 0 || option >= current.options().size()) {
                throw new SocketCommandException(ErrorCode.INVALID_COMMAND, "Invalid option");
            }

            final long responseTimeMs = Math.max(0, now() - state.getPhaseStartedAt());
            final boolean correct = current.answer().equals(current.options().get(option));
            final int points = correct ? ScoreCalculator.points(responseTimeMs, roundDurationMs(state)) : 0;
            state.getAnswers().put(playerId, new PlayerAnswer(option, responseTimeMs, correct, points));
            gameStateService.save(roomCode, state);

            final List<Player> connected = connectedPlayers(roomService.getOrException(roomCode));
            final int answered = (int) connected.stream().filter(player -> state.getAnswers().containsKey(player.getId())).count();
            socketService.broadcastToRoom(roomCode, SocketEvent.of(SocketEventType.ANSWER_RECEIVED,
                    new AnswerReceivedBroadcast(round, playerId, answered, connected.size())));

            if (answered >= connected.size()) {
                endRound(roomCode, state);
            }
        });
    }

    /**
     * Congela a etapa atual (a TV caiu). O tempo que faltava é guardado para a retomada.
     */
    public void pause(String roomCode) {
        locks.execute(roomCode, () -> {
            final Optional<GameState> found = gameStateService.find(roomCode);

            if (found.isEmpty() || !found.get().running() || found.get().paused()) {
                return;
            }

            final GameState state = found.get();
            timer.cancel(roomCode);
            state.setPausedRemainingMs(Math.max(0, state.getPhaseEndsAt() - now()));
            gameStateService.save(roomCode, state);
        });
    }

    /**
     * Retoma a etapa pausada de onde parou e reenvia a etapa atual para todos.
     */
    public void resume(String roomCode) {
        locks.execute(roomCode, () -> {
            final Optional<GameState> found = gameStateService.find(roomCode);

            if (found.isEmpty() || !found.get().running() || !found.get().paused()) {
                return;
            }

            final GameState state = found.get();
            final long remaining = state.getPausedRemainingMs();
            final long elapsed = Math.max(0, state.getPhaseEndsAt() - state.getPhaseStartedAt() - remaining);
            state.setPhaseStartedAt(now() - elapsed);
            state.setPhaseEndsAt(now() + remaining);
            state.setPausedRemainingMs(null);
            gameStateService.save(roomCode, state);

            socketService.broadcastToRoom(roomCode, phaseEvent(roomCode, state));
            sendAudioToHost(roomCode, state);
            schedule(roomCode, state);
        });
    }

    /**
     * Envia a etapa atual a quem acabou de (re)conectar. A TV recebe também a prévia da música. Só consulta o Redis
     * quando a sala tem uma partida em andamento ou pausada.
     */
    public void sendCurrentPhase(SocketPrincipal principal) {
        final boolean playing = roomService.findByCode(principal.roomCode())
                .map(room -> room.isInProgress() || room.isPaused())
                .orElse(false);

        if (!playing) {
            return;
        }

        final Optional<GameState> found = gameStateService.find(principal.roomCode());

        if (found.isEmpty() || !found.get().running()) {
            return;
        }

        final GameState state = found.get();
        socketService.sendToParticipant(principal.participantId(), phaseEvent(principal.roomCode(), state));

        if (principal.isHost() && hasAudio(state)) {
            socketService.sendToParticipant(principal.participantId(), audioEvent(state));
        }
    }

    /**
     * Prepara a partida para ser retomada depois de um reinício do backend: o timer foi perdido, então a etapa atual
     * fica pausada com o tempo que faltava e volta a correr quando a TV reconectar.
     *
     * @return {@code true} se havia uma partida em andamento para retomar
     */
    public boolean recover(String roomCode) {
        final AtomicBoolean recovered = new AtomicBoolean(false);
        locks.execute(roomCode, () -> gameStateService.find(roomCode).filter(GameState::running).ifPresent(state -> {
            if (!state.paused()) {
                state.setPausedRemainingMs(Math.max(0, state.getPhaseEndsAt() - now()));
                gameStateService.save(roomCode, state);
            }

            recovered.set(true);
        }));

        return recovered.get();
    }

    /**
     * Descarta a partida da sala (sala encerrada ou nova partida).
     */
    public void stop(String roomCode) {
        timer.cancel(roomCode);
        gameStateService.delete(roomCode);
        locks.release(roomCode);
    }

    private void advance(String roomCode, GamePhase expectedPhase, int expectedRound) {
        try {
            locks.run(roomCode, () -> {
                final Optional<GameState> found = gameStateService.find(roomCode);

                if (found.isEmpty() || found.get().paused() || found.get().getPhase() != expectedPhase
                        || found.get().getCurrentRound() != expectedRound) {
                    return;
                }

                final GameState state = found.get();
                switch (expectedPhase) {
                    case COUNTDOWN -> startPlaying(roomCode, state);
                    case PLAYING -> endRound(roomCode, state);
                    case REVEAL -> nextRoundOrFinish(roomCode, state);
                    default -> log.debug("Nothing to advance in room {}", LogSanitizer.sanitize(roomCode));
                }
            });
        } catch (BaseException | DataAccessException ex) {
            log.error("Could not advance the game in room {}: {}", LogSanitizer.sanitize(roomCode), LogSanitizer.sanitize(ex.getMessage()));
        }
    }

    private void startCountdown(String roomCode, GameState state, int roundIndex) {
        state.setCurrentRound(roundIndex);
        state.setPhase(GamePhase.COUNTDOWN);
        state.getAnswers().clear();
        state.setPausedRemainingMs(null);
        state.setPreviewUrl(fetchPreview(state.round().trackId()));
        enterPhase(state, properties.countdown().toMillis());
        gameStateService.save(roomCode, state);

        socketService.broadcastToRoom(roomCode, phaseEvent(roomCode, state));
        sendAudioToHost(roomCode, state);
        schedule(roomCode, state);
    }

    private void startPlaying(String roomCode, GameState state) {
        state.setPhase(GamePhase.PLAYING);
        enterPhase(state, roundDurationMs(state));
        gameStateService.save(roomCode, state);

        socketService.broadcastToRoom(roomCode, phaseEvent(roomCode, state));
        schedule(roomCode, state);
    }

    private void endRound(String roomCode, GameState state) {
        state.getAnswers().forEach((playerId, answer) -> {
            if (answer.correct()) {
                state.getScores().merge(playerId, answer.points(), Integer::sum);
                state.getRoundsWon().merge(playerId, 1, Integer::sum);
            }
        });
        state.setPhase(GamePhase.REVEAL);
        enterPhase(state, properties.reveal().toMillis());
        gameStateService.save(roomCode, state);

        socketService.broadcastToRoom(roomCode, phaseEvent(roomCode, state));
        schedule(roomCode, state);
    }

    private void nextRoundOrFinish(String roomCode, GameState state) throws BaseException {
        if (!state.lastRound()) {
            startCountdown(roomCode, state, state.getCurrentRound() + 1);

            return;
        }

        timer.cancel(roomCode);
        state.setPhase(GamePhase.FINISHED);
        gameStateService.save(roomCode, state);

        final Room room = roomService.getOrException(roomCode);
        playerService.applyResults(room.getPlayers(), state.getScores(), state.getRoundsWon());
        matchService.finish(room.getCurrentMatch());
        roomService.finish(room);
        notifier.broadcast(roomCode, RoomStateReason.GAME_FINISHED, null);
    }

    private void schedule(String roomCode, GameState state) {
        final GamePhase phase = state.getPhase();
        final int round = state.getCurrentRound();
        timer.schedule(roomCode, state.getPhaseEndsAt() - now(), () -> advance(roomCode, phase, round));
    }

    private void enterPhase(GameState state, long durationMs) {
        final long now = now();
        state.setPhaseStartedAt(now);
        state.setPhaseEndsAt(now + durationMs);
    }

    private SocketEvent phaseEvent(String roomCode, GameState state) {
        final int round = state.getCurrentRound() + 1;
        final long remaining = remainingMs(state);

        return switch (state.getPhase()) {
            case COUNTDOWN -> SocketEvent.of(SocketEventType.ROUND_COUNTDOWN,
                    new RoundCountdownBroadcast(round, state.totalRounds(), remaining));
            case PLAYING -> SocketEvent.of(SocketEventType.ROUND_STARTED, new RoundStartedBroadcast(round, state.totalRounds(),
                    state.round().options(), remaining, roundDurationMs(state), List.copyOf(state.getAnswers().keySet())));
            default -> SocketEvent.of(SocketEventType.ROUND_ENDED, roundEnded(roomCode, state, remaining));
        };
    }

    private RoundEndedBroadcast roundEnded(String roomCode, GameState state, long remaining) {
        final List<Player> players = roomService.findByCode(roomCode)
                .map(Room::getPlayers)
                .map(List::copyOf)
                .orElse(List.of());

        final List<RoundResultResponse> results = players.stream().map(player -> result(player, state)).toList();
        final List<RoundResultResponse> fastest = results.stream()
                .filter(RoundResultResponse::correct)
                .sorted(Comparator.comparing(RoundResultResponse::responseTimeMs))
                .limit(properties.fastestLimit())
                .toList();
        final List<ScoreResponse> scoreboard = players.stream()
                .map(player -> new ScoreResponse(player.getId(), player.getNickname(), state.getScores().getOrDefault(player.getId(), 0)))
                .sorted(Comparator.comparing(ScoreResponse::score).reversed().thenComparing(ScoreResponse::nickname))
                .toList();

        final GameRound round = state.round();

        return new RoundEndedBroadcast(state.getCurrentRound() + 1, state.totalRounds(), round.answer(), round.artist(), round.cover(),
                state.lastRound(), remaining, fastest, results, scoreboard, round.options());
    }

    private RoundResultResponse result(Player player, GameState state) {
        final PlayerAnswer answer = state.getAnswers().get(player.getId());

        if (Objects.isNull(answer)) {
            return new RoundResultResponse(player.getId(), player.getNickname(), false, false, 0, null, null);
        }

        return new RoundResultResponse(player.getId(), player.getNickname(), true, answer.correct(), answer.points(), answer.responseTimeMs(),
                answer.option());
    }

    private void sendAudioToHost(String roomCode, GameState state) {
        if (hasAudio(state)) {
            socketService.sendToHost(roomCode, audioEvent(state));
        }
    }

    private boolean hasAudio(GameState state) {
        return state.getPhase() == GamePhase.COUNTDOWN || state.getPhase() == GamePhase.PLAYING;
    }

    private SocketEvent audioEvent(GameState state) {
        final long offset = state.getPhase() == GamePhase.PLAYING ? roundDurationMs(state) - remainingMs(state) : 0;

        return SocketEvent.of(SocketEventType.ROUND_AUDIO,
                new RoundAudioBroadcast(state.getCurrentRound() + 1, state.getPreviewUrl(), Math.max(0, offset)));
    }

    private String fetchPreview(String trackId) {
        try {
            return musicService.findPreview(trackId).orElse(null);
        } catch (MusicProviderException ex) {
            log.warn("Could not fetch the preview of track {}", LogSanitizer.sanitize(trackId));

            return null;
        }
    }

    private List<Player> connectedPlayers(Room room) {
        return Objects.requireNonNullElse(room.getPlayers(), List.<Player>of()).stream().filter(Player::isConnected).toList();
    }

    private long remainingMs(GameState state) {
        return state.paused() ? state.getPausedRemainingMs() : Math.max(0, state.getPhaseEndsAt() - now());
    }

    private long roundDurationMs(GameState state) {
        return state.getRoundDuration() * MILLIS_PER_SECOND;
    }

    private long now() {
        return clock.millis();
    }
}
