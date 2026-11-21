import { AxiosError } from "axios";
import { useEffect, useState } from "react";
import type { Playlist } from "../types/protocol";
import { apiErrorMessage, resolvePlaylistLink, searchPlaylists } from "../utils/api.utils";

/** Espera depois da última tecla antes de buscar, para não chamar a Deezer a cada letra. */
const DEBOUNCE_MS = 400;

export type PlaylistSearch =
    | { status: "idle" }
    | { status: "loading" }
    | { status: "error"; message: string }
    | { status: "ready"; playlists: Playlist[] };

type Settled = Extract<PlaylistSearch, { status: "error" | "ready" }>;

/** O texto parece um link (que deve ser resolvido) e não um nome para buscar. */
export const isPlaylistLink = (text: string): boolean => /https?:\/\/|deezer\.(com|page\.link)/i.test(text);

const errorMessage = (error: unknown): string =>
    error instanceof AxiosError && error.response?.status === 404
        ? "Não encontramos uma playlist nesse link."
        : apiErrorMessage(error);

/**
 * Busca playlists pelo texto digitado: se for um link, resolve a playlist do link; se não, busca pelo nome. Só o resultado do
 * texto atual vale: respostas de buscas antigas são descartadas.
 */
export const usePlaylistSearch = (query: string): PlaylistSearch => {
    const text = query.trim();
    const [settled, setSettled] = useState<{ text: string; result: Settled } | null>(null);

    useEffect(() => {
        if (!text) {
            return;
        }

        let active = true;
        const timer = window.setTimeout(() => {
            const request = isPlaylistLink(text)
                ? resolvePlaylistLink(text).then((playlist) => [playlist])
                : searchPlaylists(text);

            request
                .then((playlists) => {
                    if (active) {
                        setSettled({ text, result: { status: "ready", playlists } });
                    }
                })
                .catch((error: unknown) => {
                    if (active) {
                        setSettled({ text, result: { status: "error", message: errorMessage(error) } });
                    }
                });
        }, DEBOUNCE_MS);

        return () => {
            active = false;
            window.clearTimeout(timer);
        };
    }, [text]);

    if (!text) {
        return { status: "idle" };
    }

    return settled?.text === text ? settled.result : { status: "loading" };
};
