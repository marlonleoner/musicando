/**
 * Efeitos sonoros curtos gerados pelo navegador (Web Audio), sem arquivos de áudio. Funcionam depois de um clique na
 * página, pela mesma regra do navegador que vale para a música. Seguem o volume escolhido na TV.
 */
import { effectiveVolume } from "./volume.utils";

let context: AudioContext | null = null;

const audioContext = (): AudioContext | null => {
    if (typeof window === "undefined" || !("AudioContext" in window)) {
        return null;
    }

    context ??= new AudioContext();
    if (context.state === "suspended") {
        void context.resume().catch(() => undefined);
    }

    return context;
};

const tone = (
    frequency: number,
    startIn: number,
    duration: number,
    type: OscillatorType = "triangle",
    volume = 0.15,
) => {
    const level = volume * effectiveVolume();
    const ctx = level > 0 ? audioContext() : null;
    if (!ctx) {
        return;
    }

    const oscillator = ctx.createOscillator();
    const gain = ctx.createGain();
    const start = ctx.currentTime + startIn;

    oscillator.type = type;
    oscillator.frequency.value = frequency;
    gain.gain.setValueAtTime(level, start);
    gain.gain.exponentialRampToValueAtTime(0.001, start + duration);
    oscillator.connect(gain).connect(ctx.destination);
    oscillator.start(start);
    oscillator.stop(start + duration);
};

/** "Tique" da contagem regressiva; mais agudo no último segundo. */
export const playTick = (last: boolean) => tone(last ? 1320 : 880, 0, 0.12, "square", 0.05);

/** Revelação com alguém acertando. */
export const playSuccess = () => {
    tone(523.25, 0, 0.18);
    tone(659.25, 0.12, 0.18);
    tone(783.99, 0.24, 0.35);
};

/** Revelação sem ninguém acertando. */
export const playFailure = () => {
    tone(220, 0, 0.25, "sawtooth", 0.06);
    tone(164.81, 0.22, 0.45, "sawtooth", 0.06);
};

/** Fim de jogo. */
export const playFanfare = () => {
    [523.25, 659.25, 783.99].forEach((frequency, index) => tone(frequency, index * 0.15, 0.25));
    tone(1046.5, 0.45, 0.8);
};
