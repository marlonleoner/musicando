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
    | "ROOM_CLOSED";

export interface Player {
    id: string;
    nickname: string;
    score: number;
    owner: boolean;
    connected: boolean;
}

export interface RoomState {
    code: string;
    status: RoomStatus;
    connected: boolean;
    players: Player[];
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

export type SocketCommandType = "GET_ROOM_STATE" | "KICK_PLAYER" | "CLOSE_ROOM";

/** Credenciais do host, recebidas ao criar a sala. */
export interface HostSession {
    id: string;
    code: string;
    secret: string;
}

export interface ApiError {
    status: number;
    code: ErrorCode;
    message: string;
}
