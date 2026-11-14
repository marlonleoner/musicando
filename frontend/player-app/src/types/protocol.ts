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
    | "GAME_STARTED";

export interface Player {
    id: string;
    nickname: string;
    score: number;
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

export type SocketEvent =
    | { type: "ROOM_STATE"; reason?: RoomStateReason; payload: RoomState; timestamp: string }
    | { type: "KICKED"; timestamp: string }
    | { type: "ERROR"; payload: ErrorPayload; timestamp: string };

export type SocketCommandType = "GET_ROOM_STATE" | "LEAVE_ROOM" | "UPDATE_SETTINGS" | "START_GAME";

/** Opções aceitas pelo backend para a configuração da partida. */
export interface MatchOptions {
    rounds: number[];
    roundDurations: number[];
    defaultRounds: number;
    defaultRoundDuration: number;
}

/** Campos enviados no `UPDATE_SETTINGS`; só os informados são alterados. */
export interface SettingsChanges {
    playlistId?: string;
    rounds?: number;
    roundDuration?: number;
}

/** Credenciais do jogador, recebidas ao entrar na sala. */
export interface PlayerSession {
    playerId: string;
    secret: string;
    roomCode: string;
}

export interface ApiError {
    status: number;
    code: ErrorCode;
    message: string;
}
