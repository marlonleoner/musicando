/// <reference types="vite/client" />

interface ImportMetaEnv {
    /** URL do backend (ex.: http://192.168.0.15:8080). Padrão: mesmo endereço da página, porta 8080. */
    readonly VITE_API_BASE_URL?: string;
}

interface ImportMeta {
    readonly env: ImportMetaEnv;
}
