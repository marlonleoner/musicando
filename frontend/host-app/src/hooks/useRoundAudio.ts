import { useCallback, useEffect, useRef, useState } from "react";
import type { RoomAudio, RoundView } from "../types/protocol";
import { effectiveVolume, subscribeVolume } from "../utils/volume.utils";

/** Duração do fade out no fim da revelação. */
const FADE_MS = 2000;

/** Fade in curto no começo, para a música não entrar cortando uma frase no meio. */
const FADE_IN_MS = 300;

const FADE_STEP_MS = 50;

/**
 * Ponto da prévia (em segundos) em que a música começa: a fração sorteada pelo backend aplicada ao trecho que ainda
 * deixa tocar a rodada e a revelação inteiras. Usa a duração real da prévia, então uma prévia mais curta que o normal
 * só reduz o trecho possível; sem a duração, começa do início.
 */
const startPosition = (audio: RoomAudio | null, durationSec: number): number =>
    audio && Number.isFinite(durationSec) ? audio.startFraction * Math.max(0, durationSec - audio.playMs / 1000) : 0;

/**
 * Toca a prévia da rodada a partir do ponto sorteado, na posição certa mesmo depois de uma reconexão. A música entra
 * com um fade in curto, continua durante a revelação e termina com um fade out junto com o fim da revelação. Se o
 * navegador bloquear o som (página recarregada sem nenhum clique), `blocked` fica verdadeiro até `unlock` ser chamado a
 * partir de um clique.
 */
export const useRoundAudio = (audio: RoomAudio | null, round: RoundView | null) => {
    const elementRef = useRef<HTMLAudioElement | null>(null);
    const roundRef = useRef<RoundView | null>(round);
    const audioRef = useRef<RoomAudio | null>(audio);
    const fadeInRef = useRef<number | undefined>(undefined);
    /** Fração do volume escolhido que está tocando (cai até zero no fade out). */
    const gainRef = useRef(1);
    const [blocked, setBlocked] = useState(false);
    const previewUrl = audio && round && audio.round === round.round ? audio.previewUrl : null;
    const playing = round?.phase === "PLAYING" && Boolean(previewUrl);
    const audibleRound = previewUrl && (round?.phase === "PLAYING" || round?.phase === "REVEAL") ? round.round : null;
    const fadingRound = previewUrl && round?.phase === "REVEAL" ? round.round : null;

    useEffect(() => {
        roundRef.current = round;
    }, [round]);

    useEffect(() => {
        audioRef.current = audio;
    }, [audio]);

    useEffect(
        () =>
            subscribeVolume(() => {
                if (elementRef.current) {
                    elementRef.current.volume = effectiveVolume() * gainRef.current;
                }
            }),
        [],
    );

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
        element.currentTime = startPosition(audioRef.current, element.duration) + Math.max(0, elapsedMs / 1000);

        window.clearInterval(fadeInRef.current);
        gainRef.current = 0;
        element.volume = 0;
        const fadeStartedAt = Date.now();
        fadeInRef.current = window.setInterval(() => {
            gainRef.current = Math.min(1, (Date.now() - fadeStartedAt) / FADE_IN_MS);
            element.volume = effectiveVolume() * gainRef.current;
            if (gainRef.current >= 1) {
                window.clearInterval(fadeInRef.current);
            }
        }, FADE_STEP_MS);

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

        const element = elementRef.current;
        if (roundRef.current?.phase === "PLAYING") {
            // O ponto de início depende da duração da prévia: se ela ainda não carregou, espera.
            if (element && !Number.isFinite(element.duration)) {
                element.addEventListener("loadedmetadata", play, { once: true });
            } else {
                play();
            }
        }

        return () => {
            element?.removeEventListener("loadedmetadata", play);
            window.clearInterval(fadeInRef.current);
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
                const initialGain = gainRef.current;
                const startedAt = Date.now();
                fade = window.setInterval(() => {
                    const progress = Math.min(1, (Date.now() - startedAt) / FADE_MS);
                    gainRef.current = initialGain * (1 - progress);
                    element.volume = effectiveVolume() * gainRef.current;
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
