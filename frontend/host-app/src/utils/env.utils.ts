const API_PORT = 8080;

const PLAYER_APP_PORT = 5174;

const LOCAL_HOSTNAMES = ["localhost", "127.0.0.1", "[::1]"];

const sameHostUrl = (port: number) => `${window.location.protocol}//${window.location.hostname}:${port}`;

const trimSlash = (url: string) => url.replace(/\/+$/, "");

/** URL do backend. Sem configuração, usa o mesmo endereço pelo qual a página foi aberta, na porta 8080. */
export const apiBaseUrl = (): string => trimSlash(import.meta.env.VITE_API_BASE_URL || sameHostUrl(API_PORT));

/** URL do WebSocket, derivada da URL do backend (http → ws, https → wss). */
export const socketUrl = (): string => `${apiBaseUrl().replace(/^http/, "ws")}/ws`;

/** Link do player-app que o QR code abre, já com o código da sala. */
export const playerJoinUrl = (code: string): string => {
    const base = trimSlash(import.meta.env.VITE_PLAYER_APP_URL || sameHostUrl(PLAYER_APP_PORT));

    return `${base}/?code=${encodeURIComponent(code)}`;
};

/**
 * Indica se o link do QR code aponta para "localhost", que não abre no celular. Acontece quando a TV abre o host-app
 * por localhost e não há VITE_PLAYER_APP_URL configurada.
 */
export const isPlayerUrlUnreachable = (): boolean =>
    !import.meta.env.VITE_PLAYER_APP_URL && LOCAL_HOSTNAMES.includes(window.location.hostname);
