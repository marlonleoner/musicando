import { useCallback, useEffect, useRef, useState } from "react";
import type { RoomAudio, RoundView } from "../types/protocol";

/** Duração do fade out no fim da revelação. */
const FADE_MS = 2000;

const FADE_STEP_MS = 50;

/**
 * Toca a prévia da rodada a partir do início da música, na posição certa mesmo depois de uma reconexão. A música
 * continua durante a revelação e termina com um fade out junto com o fim da revelação. Se o navegador bloquear o som
 * (página recarregada sem nenhum clique), `blocked` fica verdadeiro até `unlock` ser chamado a partir de um clique.
 */
export const useRoundAudio = (audio: RoomAudio | null, round: RoundView | null) => {
    const elementRef = useRef<HTMLAudioElement | null>(null);
    const roundRef = useRef<RoundView | null>(round);
    const [blocked, setBlocked] = useState(false);
    const previewUrl = audio && round && audio.round === round.round ? audio.previewUrl : null;
    const playing = round?.phase === "PLAYING" && Boolean(previewUrl);
    const audibleRound = previewUrl && (round?.phase === "PLAYING" || round?.phase === "REVEAL") ? round.round : null;
    const fadingRound = previewUrl && round?.phase === "REVEAL" ? round.round : null;

    useEffect(() => {
        roundRef.current = round;
    }, [round]);

    useEffect(() => {
        if (!previewUrl) {
            return;
        }

        if (!elementRef.current) {
            elementRef.current = new Audio();
        }

        const element = elementRef.current;
        if (element.src !== previewUrl) {
            element.preload = "auto";
            element.src = previewUrl;
            element.load();
        }
    }, [previewUrl]);

    const play = useCallback(() => {
        const element = elementRef.current;
        const current = roundRef.current;
        if (!element || !current || current.phase !== "PLAYING") {
            return;
        }

        const elapsedMs = current.phaseDurationMs - (current.endsAt - Date.now());
        element.volume = 1;
        element.currentTime = Math.max(0, elapsedMs / 1000);
        element
            .play()
            .then(() => setBlocked(false))
            .catch((error: unknown) => {
                if (error instanceof DOMException && error.name === "NotAllowedError") {
                    setBlocked(true);
                }
            });
    }, []);

    // A música começa quando a rodada passa a valer e segue tocando na revelação da mesma rodada. Quem reconecta
    // já na revelação não ouve a música (ela só voltaria para terminar em seguida).
    useEffect(() => {
        if (audibleRound === null) {
            return;
        }

        if (roundRef.current?.phase === "PLAYING") {
            play();
        }

        const element = elementRef.current;

        return () => {
            element?.pause();
        };
    }, [audibleRound, play]);

    // Fade out nos últimos instantes da revelação.
    useEffect(() => {
        const element = elementRef.current;
        const current = roundRef.current;
        if (fadingRound === null || !element || !current) {
            return;
        }

        let fade: number | undefined;
        const start = window.setTimeout(
            () => {
                const initialVolume = element.volume;
                const startedAt = Date.now();
                fade = window.setInterval(() => {
                    const progress = Math.min(1, (Date.now() - startedAt) / FADE_MS);
                    element.volume = initialVolume * (1 - progress);
                    if (progress >= 1) {
                        window.clearInterval(fade);
                        element.pause();
                    }
                }, FADE_STEP_MS);
            },
            Math.max(0, current.endsAt - FADE_MS - Date.now()),
        );

        return () => {
            window.clearTimeout(start);
            window.clearInterval(fade);
        };
    }, [fadingRound]);

    return { blocked: blocked && playing, unlock: play };
};
