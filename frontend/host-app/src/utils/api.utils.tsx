import axios, { AxiosError } from "axios";
import type { ApiError, ErrorCode, ErrorPayload, HostSession } from "../types/protocol";
import { apiBaseUrl } from "./env.utils";

export const api = axios.create({
    baseURL: apiBaseUrl(),
    timeout: 5000,
    headers: {
        "Content-Type": "application/json",
    },
});

export const createRoom = async (): Promise<HostSession> => {
    const { data } = await api.post<HostSession>("/room");

    return data;
};

const MESSAGES: Partial<Record<ErrorCode, string>> = {
    SERVICE_UNAVAILABLE: "O servidor está temporariamente indisponível. Tente novamente em instantes.",
    RATE_LIMITED: "Muitas tentativas em pouco tempo. Aguarde um instante e tente de novo.",
};

/** Mensagem amigável para erros de comandos enviados pelo socket. */
export const commandErrorMessage = (error: ErrorPayload): string => MESSAGES[error.code] ?? error.message;

/** Mensagem amigável para falhas de chamadas à API. */
export const apiErrorMessage = (error: unknown): string => {
    if (error instanceof AxiosError) {
        const body = error.response?.data as Partial<ApiError> | undefined;
        if (!error.response) {
            return "Não foi possível falar com o servidor. Verifique se ele está no ar.";
        }

        if (body?.code && MESSAGES[body.code]) {
            return MESSAGES[body.code] as string;
        }

        if (body?.message) {
            return body.message;
        }
    }

    return "Algo deu errado. Tente novamente.";
};
