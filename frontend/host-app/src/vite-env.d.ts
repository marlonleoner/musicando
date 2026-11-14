/// <reference types="vite/client" />

interface ImportMetaEnv {
    /** URL do backend (ex.: http://192.168.0.15:8080). Padrão: mesmo endereço da página, porta 8080. */
    readonly VITE_API_BASE_URL?: string;
    /** URL do player-app usada no QR code (ex.: https://jogar.musicando.com.br). Padrão: mesmo endereço da página, porta 5174. */
    readonly VITE_PLAYER_APP_URL?: string;
}

interface ImportMeta {
    readonly env: ImportMetaEnv;
}
