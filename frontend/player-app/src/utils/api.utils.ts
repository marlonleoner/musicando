import axios, { AxiosError } from "axios";
import type { ApiError, ErrorCode, PlayerSession } from "../types/protocol";
import { apiBaseUrl } from "./env.utils";

const api = axios.create({
    baseURL: apiBaseUrl(),
    timeout: 5000,
    headers: {
        "Content-Type": "application/json",
    },
});

export const joinRoom = async (code: string, nickname: string): Promise<PlayerSession> => {
    const { data } = await api.post<PlayerSession>("/room/join", { code, nickname });

    return data;
};

const MESSAGES: Partial<Record<ErrorCode, string>> = {
    ROOM_NOT_FOUND: "Sala não encontrada. Confira o código na TV.",
    ROOM_NOT_JOINABLE: "Essa sala já começou a partida ou foi encerrada.",
    ROOM_FULL: "A sala está cheia.",
    NICKNAME_TAKEN: "Esse apelido já está em uso nessa sala. Escolha outro.",
    INVALID_NICKNAME: "Use de 2 a 16 caracteres: letras, números, espaço, ponto, hífen ou sublinhado.",
};

/** Mensagem amigável para falhas de chamadas à API. */
export const apiErrorMessage = (error: unknown): string => {
    if (error instanceof AxiosError) {
        if (!error.response) {
            return "Não foi possível falar com o servidor. Verifique sua conexão.";
        }

        const body = error.response.data as Partial<ApiError> | undefined;
        if (body?.code && MESSAGES[body.code]) {
            return MESSAGES[body.code] as string;
        }

        if (body?.message) {
            return body.message;
        }
    }

    return "Algo deu errado. Tente novamente.";
};
