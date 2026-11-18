const API_PORT = 8080;

const trimSlash = (url: string) => url.replace(/\/+$/, "");

/**
 * URL do backend. Sem configuração, usa o mesmo endereço pelo qual a página foi aberta, na porta 8080. Um caminho
 * relativo (ex.: "/api") indica que o backend responde na mesma origem da página, atrás de um proxy.
 */
export const apiBaseUrl = (): string =>
    trimSlash(
        import.meta.env.VITE_API_BASE_URL || `${window.location.protocol}//${window.location.hostname}:${API_PORT}`,
    );

/**
 * URL do WebSocket. Com o backend na mesma origem, é "/ws" no endereço da página; senão, é derivada da URL do backend
 * (http → ws, https → wss).
 */
export const socketUrl = (): string => {
    const base = apiBaseUrl();
    if (base.startsWith("/")) {
        return `${window.location.protocol === "https:" ? "wss:" : "ws:"}//${window.location.host}/ws`;
    }

    return `${base.replace(/^http/, "ws")}/ws`;
};

/** Código da sala recebido pelo link do QR code (?code=ABC123). */
export const codeFromUrl = (): string => new URLSearchParams(window.location.search).get("code")?.toUpperCase() ?? "";

/** Remove o código da URL depois de lido, para que recarregar a página não reinicie a entrada. */
export const clearCodeFromUrl = (): void => {
    if (window.location.search) {
        window.history.replaceState(null, "", window.location.pathname);
    }
};
