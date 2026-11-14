import { createContext } from "react";
import type { ConnectionStatus } from "../hooks/useHostSocket";
import type { HostSession, RoomAudio, RoomState, RoundView } from "../types/protocol";

export interface IGameContext {
    session: HostSession | null;
    room: RoomState | null;
    /** Rodada atual da partida; nula fora de uma partida. */
    round: RoundView | null;
    /** Prévia da música da rodada, recebida só pela TV. */
    audio: RoomAudio | null;
    connection: ConnectionStatus;
    creatingRoom: boolean;
    createRoom: () => Promise<void>;
    kickPlayer: (playerId: string) => void;
    closeRoom: () => void;
    reclaimScreen: () => void;
}

export const GameContext = createContext<IGameContext>(null as unknown as IGameContext);

export interface IThemeContext {
    theme: "dark" | "light";
    toggleTheme: () => void;
    setTheme: (newTheme: "dark" | "light") => void;
}

export const ThemeContext = createContext<IThemeContext | undefined>(undefined);
