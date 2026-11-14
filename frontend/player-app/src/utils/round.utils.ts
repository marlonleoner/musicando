import type { GameEvent, RoundView, Score } from "../types/protocol";

/** Placar a comparar na próxima revelação: o da última revelação, mantido ao longo da rodada seguinte. */
const carriedScoreboard = (current: RoundView | null): Score[] =>
    current?.ended?.scoreboard ?? current?.previousScoreboard ?? [];

/**
 * Atualiza a rodada atual com um evento do jogo. Os tempos chegam como "quanto falta" e viram instantes no relógio
 * deste aparelho, para não depender do relógio do servidor.
 */
export const applyGameEvent = (current: RoundView | null, event: GameEvent, now = Date.now()): RoundView | null => {
    switch (event.type) {
        case "ROUND_COUNTDOWN":
            return {
                phase: "COUNTDOWN",
                round: event.payload.round,
                totalRounds: event.payload.totalRounds,
                endsAt: now + event.payload.durationMs,
                phaseDurationMs: event.payload.durationMs,
                options: [],
                answeredPlayerIds: [],
                ended: null,
                previousScoreboard: carriedScoreboard(current),
            };
        case "ROUND_STARTED":
            return {
                phase: "PLAYING",
                round: event.payload.round,
                totalRounds: event.payload.totalRounds,
                endsAt: now + event.payload.durationMs,
                phaseDurationMs: event.payload.roundDurationMs,
                options: event.payload.options,
                answeredPlayerIds: event.payload.answeredPlayerIds,
                ended: null,
                previousScoreboard: carriedScoreboard(current),
            };
        case "ANSWER_RECEIVED":
            if (
                !current ||
                current.round !== event.payload.round ||
                current.answeredPlayerIds.includes(event.payload.playerId)
            ) {
                return current;
            }

            return { ...current, answeredPlayerIds: [...current.answeredPlayerIds, event.payload.playerId] };
        case "ROUND_ENDED":
            return {
                phase: "REVEAL",
                round: event.payload.round,
                totalRounds: event.payload.totalRounds,
                endsAt: now + event.payload.durationMs,
                phaseDurationMs: event.payload.durationMs,
                options: current?.round === event.payload.round ? current.options : [],
                answeredPlayerIds: current?.round === event.payload.round ? current.answeredPlayerIds : [],
                ended: event.payload,
                previousScoreboard: current?.round === event.payload.round ? current.previousScoreboard : [],
            };
        default:
            return current;
    }
};

export const formatPoints = (points: number): string => points.toLocaleString("pt-BR");

export const formatSeconds = (ms: number | null): string =>
    ms === null
        ? "–"
        : `${(ms / 1000).toLocaleString("pt-BR", { minimumFractionDigits: 1, maximumFractionDigits: 1 })} s`;

export const ordinal = (position: number): string => `${position}º`;

/** Quantas posições o jogador subiu (positivo) ou desceu (negativo) desde a rodada anterior. */
export const positionChange = (previous: Score[], current: Score[], playerId: string): number => {
    const before = previous.findIndex((score) => score.playerId === playerId);
    const after = current.findIndex((score) => score.playerId === playerId);

    return before < 0 || after < 0 ? 0 : before - after;
};
