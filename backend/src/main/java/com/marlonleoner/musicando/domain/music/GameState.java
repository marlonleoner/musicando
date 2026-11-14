package com.marlonleoner.musicando.domain.music;

import com.marlonleoner.musicando.domain.enums.GamePhase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Estado de uma partida em andamento, guardado no Redis. Os instantes são em milissegundos desde a época.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameState {

    /** Partida a que o estado pertence. */
    private String matchId;

    /** Duração de cada rodada, em segundos. */
    private int roundDuration;

    /** Rodadas sorteadas. */
    @Builder.Default
    private List<GameRound> rounds = new ArrayList<>();

    /** Índice da rodada atual (começa em 0). */
    private int currentRound;

    /** Etapa da rodada atual; nula antes da primeira contagem. */
    private GamePhase phase;

    /** Início da etapa atual, descontado o tempo em que a partida ficou pausada. */
    private long phaseStartedAt;

    /** Fim previsto da etapa atual. */
    private long phaseEndsAt;

    /** Tempo que faltava para o fim da etapa quando a partida foi pausada; nulo quando não está pausada. */
    private Long pausedRemainingMs;

    /** Prévia da música da rodada atual; nula quando o provedor não tem prévia disponível. */
    private String previewUrl;

    /** Respostas da rodada atual, por jogador. */
    @Builder.Default
    private Map<String, PlayerAnswer> answers = new HashMap<>();

    /** Pontuação acumulada, por jogador. */
    @Builder.Default
    private Map<String, Integer> scores = new HashMap<>();

    /** Rodadas acertadas, por jogador. */
    @Builder.Default
    private Map<String, Integer> roundsWon = new HashMap<>();

    public GameRound round() {
        return rounds.get(currentRound);
    }

    public int totalRounds() {
        return rounds.size();
    }

    public boolean lastRound() {
        return currentRound >= rounds.size() - 1;
    }

    public boolean paused() {
        return Objects.nonNull(pausedRemainingMs);
    }

    public boolean running() {
        return Objects.nonNull(phase) && phase != GamePhase.FINISHED;
    }
}
