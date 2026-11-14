import axios, { AxiosError } from "axios";
import type { ApiError, ErrorCode, ErrorPayload, MatchOptions, PlayerSession, Playlist } from "../types/protocol";
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

/** A Deezer pode levar alguns segundos para responder. */
const MUSIC_TIMEOUT_MS = 10000;

export const getFeaturedPlaylists = async (): Promise<Playlist[]> => {
    const { data } = await api.get<Playlist[]>("/music/playlists/featured", { timeout: MUSIC_TIMEOUT_MS });

    return data;
};

export const getMatchOptions = async (): Promise<MatchOptions> => {
    const { data } = await api.get<MatchOptions>("/music/options");

    return data;
};

const MESSAGES: Partial<Record<ErrorCode, string>> = {
    ROOM_NOT_FOUND: "Sala não encontrada. Confira o código na TV.",
    ROOM_NOT_JOINABLE: "Essa sala já começou a partida ou foi encerrada.",
    ROOM_FULL: "A sala está cheia.",
    NICKNAME_TAKEN: "Esse apelido já está em uso nessa sala. Escolha outro.",
    INVALID_NICKNAME: "Use de 2 a 16 caracteres: letras, números, espaço, ponto, hífen ou sublinhado.",
    FORBIDDEN: "Somente o VIP pode fazer isso.",
    INVALID_SETTINGS: "Confira a configuração: escolha uma playlist e as opções disponíveis.",
    PLAYLIST_NOT_FOUND: "Playlist não encontrada.",
    PLAYLIST_TOO_SMALL: "Essa playlist não tem músicas suficientes. Escolha outra ou diminua as rodadas.",
    NOT_ENOUGH_PLAYERS: "Ainda não há jogadores suficientes conectados para começar.",
    GAME_ALREADY_STARTED: "A partida já começou.",
    MUSIC_PROVIDER_UNAVAILABLE: "A Deezer não respondeu. Tente novamente em instantes.",
    ANSWER_NOT_ACCEPTED: "Sua resposta não foi aceita: o tempo da rodada acabou.",
    SERVICE_UNAVAILABLE: "O servidor está temporariamente indisponível. Tente novamente em instantes.",
};

/** Mensagem amigável para erros de comandos enviados pelo socket. */
export const commandErrorMessage = (error: ErrorPayload): string => MESSAGES[error.code] ?? error.message;

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
