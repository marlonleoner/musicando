const API_PORT = 8080;

const trimSlash = (url: string) => url.replace(/\/+$/, "");

/** URL do backend. Sem configuração, usa o mesmo endereço pelo qual a página foi aberta, na porta 8080. */
export const apiBaseUrl = (): string =>
    trimSlash(
        import.meta.env.VITE_API_BASE_URL || `${window.location.protocol}//${window.location.hostname}:${API_PORT}`,
    );

/** URL do WebSocket, derivada da URL do backend (http → ws, https → wss). */
export const socketUrl = (): string => `${apiBaseUrl().replace(/^http/, "ws")}/ws`;

/** Código da sala recebido pelo link do QR code (?code=ABC123). */
export const codeFromUrl = (): string => new URLSearchParams(window.location.search).get("code")?.toUpperCase() ?? "";

/** Remove o código da URL depois de lido, para que recarregar a página não reinicie a entrada. */
export const clearCodeFromUrl = (): void => {
    if (window.location.search) {
        window.history.replaceState(null, "", window.location.pathname);
    }
};
