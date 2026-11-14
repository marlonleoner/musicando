import axios, { AxiosError } from "axios";
import type { ApiError, HostSession } from "../types/protocol";
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

/** Mensagem amigável para falhas de chamadas à API. */
export const apiErrorMessage = (error: unknown): string => {
    if (error instanceof AxiosError) {
        const body = error.response?.data as Partial<ApiError> | undefined;
        if (!error.response) {
            return "Não foi possível falar com o servidor. Verifique se ele está no ar.";
        }

        if (body?.message) {
            return body.message;
        }
    }

    return "Algo deu errado. Tente novamente.";
};
