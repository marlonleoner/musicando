import { createContext } from "react";
import type { ConnectionStatus } from "../hooks/usePlayerSocket";
import type { PlayerSession, RoomState, SettingsChanges } from "../types/protocol";

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
    /** Altera a configuração da partida (somente VIP). A tela muda quando chega o novo estado da sala. */
    updateSettings: (changes: SettingsChanges) => void;
    /** Inicia a partida (somente VIP). */
    startGame: () => void;
    /** Verdadeiro entre o envio do `START_GAME` e a resposta do backend. */
    starting: boolean;
    reclaimTab: () => void;
    dismissExitReason: () => void;
}

export const PlayerContext = createContext<IPlayerContext>(null as unknown as IPlayerContext);
