import type { PlayerSession } from "../types/protocol";

const PLAYER_SESSION_KEY = "@musicando/player-session";

export const loadPlayerSession = (): PlayerSession | null => {
    try {
        const value = localStorage.getItem(PLAYER_SESSION_KEY);
        const session = value ? (JSON.parse(value) as Partial<PlayerSession>) : null;

        return session?.playerId && session.secret && session.roomCode ? (session as PlayerSession) : null;
    } catch {
        return null;
    }
};

export const savePlayerSession = (session: PlayerSession): void => {
    try {
        localStorage.setItem(PLAYER_SESSION_KEY, JSON.stringify(session));
    } catch {
        // Armazenamento indisponível: o jogo funciona, mas não será retomado ao recarregar a página.
    }
};

export const clearPlayerSession = (): void => {
    try {
        localStorage.removeItem(PLAYER_SESSION_KEY);
    } catch {
        // Nada a fazer
    }
};
