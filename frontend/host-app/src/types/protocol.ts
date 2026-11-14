/**
 * Contrato com o backend (REST e WebSocket).
 */

export type RoomStatus = "WAITING" | "IN_PROGRESS" | "PAUSED" | "FINISHED" | "CLOSED";

export type ErrorCode =
    | "INVALID_CREDENTIALS"
    | "FORBIDDEN"
    | "ROOM_NOT_FOUND"
    | "PLAYER_NOT_FOUND"
    | "ROOM_NOT_JOINABLE"
    | "ROOM_FULL"
    | "INVALID_NICKNAME"
    | "NICKNAME_TAKEN"
    | "INVALID_COMMAND"
    | "INVALID_SETTINGS"
    | "PLAYLIST_NOT_FOUND"
    | "PLAYLIST_TOO_SMALL"
    | "NOT_ENOUGH_PLAYERS"
    | "GAME_ALREADY_STARTED"
    | "MUSIC_PROVIDER_UNAVAILABLE"
    | "ANSWER_NOT_ACCEPTED"
    | "INTERNAL_ERROR";

export type RoomStateReasonType =
    | "SNAPSHOT"
    | "HOST_CONNECTED"
    | "HOST_DISCONNECTED"
    | "PLAYER_JOINED"
    | "PLAYER_CONNECTED"
    | "PLAYER_DISCONNECTED"
    | "PLAYER_LEFT"
    | "PLAYER_KICKED"
    | "ROOM_CLOSED"
    | "SETTINGS_UPDATED"
    | "GAME_STARTED"
    | "GAME_FINISHED"
    | "PLAY_AGAIN";

export interface Player {
    id: string;
    nickname: string;
    score: number;
    roundsWon: number;
    owner: boolean;
    connected: boolean;
}

export interface Playlist {
    id: string;
    name: string;
    cover: string | null;
    trackCount: number;
}

/** Configuração da partida escolhida pelo VIP. `playlist` fica nulo até a primeira escolha. */
export interface MatchSettings {
    playlist: Playlist | null;
    rounds: number;
    roundDuration: number;
}

export interface RoomState {
    code: string;
    status: RoomStatus;
    connected: boolean;
    players: Player[];
    settings: MatchSettings | null;
}

export interface RoomStateReason {
    type: RoomStateReasonType;
    participantId?: string;
}

export interface ErrorPayload {
    code: ErrorCode;
    message: string;
}

export interface RoundCountdownPayload {
    round: number;
    totalRounds: number;
    durationMs: number;
}

export interface RoundStartedPayload {
    round: number;
    totalRounds: number;
    options: string[];
    durationMs: number;
    roundDurationMs: number;
    answeredPlayerIds: string[];
}

export interface RoundAudioPayload {
    round: number;
    previewUrl: string | null;
    offsetMs: number;
}

export interface AnswerReceivedPayload {
    round: number;
    playerId: string;
    answeredCount: number;
    totalPlayers: number;
}

export interface RoundResult {
    playerId: string;
    nickname: string;
    answered: boolean;
    correct: boolean;
    points: number;
    responseTimeMs: number | null;
}

export interface Score {
    playerId: string;
    nickname: string;
    score: number;
}

export interface RoundEndedPayload {
    round: number;
    totalRounds: number;
    answer: string;
    lastRound: boolean;
    durationMs: number;
    fastest: RoundResult[];
    results: RoundResult[];
    scoreboard: Score[];
}

export type GameEvent =
    | { type: "ROUND_COUNTDOWN"; payload: RoundCountdownPayload; timestamp: string }
    | { type: "ROUND_STARTED"; payload: RoundStartedPayload; timestamp: string }
    | { type: "ROUND_AUDIO"; payload: RoundAudioPayload; timestamp: string }
    | { type: "ANSWER_RECEIVED"; payload: AnswerReceivedPayload; timestamp: string }
    | { type: "ROUND_ENDED"; payload: RoundEndedPayload; timestamp: string };

export type SocketEvent =
    | GameEvent
    | { type: "ROOM_STATE"; reason?: RoomStateReason; payload: RoomState; timestamp: string }
    | { type: "KICKED"; timestamp: string }
    | { type: "ERROR"; payload: ErrorPayload; timestamp: string };

export type SocketCommandType = "GET_ROOM_STATE" | "KICK_PLAYER" | "CLOSE_ROOM";

/** Credenciais do host, recebidas ao criar a sala. */
export interface HostSession {
    id: string;
    code: string;
    secret: string;
}

/** Rodada atual, montada a partir dos eventos do jogo. `endsAt` é o fim da etapa no relógio deste aparelho. */
export interface RoundView {
    phase: "COUNTDOWN" | "PLAYING" | "REVEAL";
    round: number;
    totalRounds: number;
    endsAt: number;
    phaseDurationMs: number;
    options: string[];
    answeredPlayerIds: string[];
    ended: RoundEndedPayload | null;
}

/** Prévia da rodada para a TV tocar. */
export interface RoomAudio {
    round: number;
    previewUrl: string | null;
}

export interface ApiError {
    status: number;
    code: ErrorCode;
    message: string;
}
