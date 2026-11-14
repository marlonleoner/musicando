import type { HostSession } from "../types/protocol";

const HOST_SESSION_KEY = "@musicando/host-session";

export const loadHostSession = (): HostSession | null => {
    try {
        const value = localStorage.getItem(HOST_SESSION_KEY);
        const session = value ? (JSON.parse(value) as Partial<HostSession>) : null;

        return session?.id && session.code && session.secret ? (session as HostSession) : null;
    } catch {
        return null;
    }
};

export const saveHostSession = (session: HostSession): void => {
    try {
        localStorage.setItem(HOST_SESSION_KEY, JSON.stringify(session));
    } catch {
        // Armazenamento indisponível: a sala funciona, mas não será retomada ao recarregar a página.
    }
};

export const clearHostSession = (): void => {
    try {
        localStorage.removeItem(HOST_SESSION_KEY);
    } catch {
        // Nada a fazer
    }
};
