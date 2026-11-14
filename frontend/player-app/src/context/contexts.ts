import { createContext } from "react";
import type { ConnectionStatus } from "../hooks/usePlayerSocket";
import type { PlayerSession, RoomState } from "../types/protocol";

/** Motivo pelo qual o jogador saiu da sala sem ter pedido, exibido na tela de entrada. */
export type ExitReason = "KICKED" | "ROOM_CLOSED" | "SESSION_EXPIRED" | null;

export interface IPlayerContext {
    session: PlayerSession | null;
    room: RoomState | null;
    connection: ConnectionStatus;
    exitReason: ExitReason;
    joining: boolean;
    join: (code: string, nickname: string) => Promise<void>;
    leave: () => void;
    reclaimTab: () => void;
    dismissExitReason: () => void;
}

export const PlayerContext = createContext<IPlayerContext>(null as unknown as IPlayerContext);
