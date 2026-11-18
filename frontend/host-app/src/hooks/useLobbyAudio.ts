import { useCallback, useEffect, useRef, useState } from "react";
import { effectiveVolume, subscribeVolume } from "../utils/volume.utils";

/** Volume da música do lobby em relação ao volume escolhido: ela é só uma trilha de fundo. */
const LOBBY_GAIN = 0.6;

/** Quanto de cada prévia toca antes de passar para a próxima. */
const SEGMENT_MS = 20000;

const FADE_IN_MS = 1500;

const FADE_OUT_MS = 2000;

/** Fade out ao sair do lobby (início da partida) ou ao trocar de playlist. */
const STOP_FADE_MS = 1200;

const STEP_MS = 50;

/**
 * Toca de fundo, em sequência e em loop, as prévias sorteadas da playlist do lobby. Cada prévia toca por alguns
 * segundos, com fade de entrada e de saída; prévias que não carregam são puladas. Ao sair do lobby ou trocar de
 * playlist, a música atual termina com um fade out. Se o navegador bloquear o som, `blocked` fica verdadeiro até
 * `unlock` ser chamado a partir de um clique.
 */
export const useLobbyAudio = (previewUrls: string[] | null, active: boolean) => {
    const elementRef = useRef<HTMLAudioElement | null>(null);
    const [blocked, setBlocked] = useState(false);
    const urls = active && previewUrls && previewUrls.length > 0 ? previewUrls : null;

    useEffect(() => {
        if (!urls) {
            return;
        }

        // Um elemento por lista: o fade out da lista anterior não interfere na nova.
        const element = new Audio();
        elementRef.current = element;
        let index = 0;
        let failures = 0;
        let gain = 0;

        const apply = (value: number) => {
            gain = value;
            element.volume = effectiveVolume() * LOBBY_GAIN * gain;
        };

        const playAt = (next: number) => {
            if (failures >= urls.length) {
                element.pause();

                return;
            }

            index = next % urls.length;
            apply(0);
            element.src = urls[index];
            element
                .play()
                .then(() => setBlocked(false))
                .catch((error: unknown) => {
                    // Prévias que não carregam são tratadas no evento "error".
                    if (error instanceof DOMException && error.name === "NotAllowedError") {
                        setBlocked(true);
                    }
                });
        };

        const onPlaying = () => {
            failures = 0;
        };
        const onEnded = () => playAt(index + 1);
        const onError = () => {
            failures += 1;
            playAt(index + 1);
        };

        const step = window.setInterval(() => {
            if (element.paused) {
                return;
            }

            const elapsed = element.currentTime * 1000;
            if (elapsed >= SEGMENT_MS) {
                playAt(index + 1);

                return;
            }

            apply(Math.max(0, Math.min(1, elapsed / FADE_IN_MS, (SEGMENT_MS - elapsed) / FADE_OUT_MS)));
        }, STEP_MS);

        element.addEventListener("playing", onPlaying);
        element.addEventListener("ended", onEnded);
        element.addEventListener("error", onError);
        const unsubscribe = subscribeVolume(() => apply(gain));
        playAt(0);

        return () => {
            window.clearInterval(step);
            unsubscribe();
            element.removeEventListener("playing", onPlaying);
            element.removeEventListener("ended", onEnded);
            element.removeEventListener("error", onError);

            const from = gain;
            const startedAt = Date.now();
            const fade = window.setInterval(() => {
                const progress = Math.min(1, (Date.now() - startedAt) / STOP_FADE_MS);
                apply(from * (1 - progress));
                if (progress >= 1) {
                    window.clearInterval(fade);
                    element.pause();
                    element.removeAttribute("src");
                }
            }, STEP_MS);
        };
    }, [urls]);

    const unlock = useCallback(() => {
        elementRef.current
            ?.play()
            .then(() => setBlocked(false))
            .catch(() => undefined);
    }, []);

    return { blocked: blocked && Boolean(urls), unlock };
};
