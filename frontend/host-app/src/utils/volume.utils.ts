/**
 * Volume escolhido na TV, guardado no navegador. Vale para a música das rodadas, para a música do lobby e para os
 * efeitos sonoros.
 */
export interface VolumeSettings {
    /** De 0 a 1. */
    level: number;
    muted: boolean;
}

const VOLUME_KEY = "@musicando/volume";

const DEFAULT_SETTINGS: VolumeSettings = { level: 0.7, muted: false };

const clamp = (value: number) => Math.min(1, Math.max(0, value));

const load = (): VolumeSettings => {
    try {
        const value = localStorage.getItem(VOLUME_KEY);
        const stored = value ? (JSON.parse(value) as Partial<VolumeSettings>) : null;

        return typeof stored?.level === "number"
            ? { level: clamp(stored.level), muted: stored.muted === true }
            : DEFAULT_SETTINGS;
    } catch {
        return DEFAULT_SETTINGS;
    }
};

let current = load();

const listeners = new Set<() => void>();

export const getVolumeSettings = (): VolumeSettings => current;

/** Volume a aplicar nos sons, de 0 a 1 (zero quando está mudo). */
export const effectiveVolume = (): number => (current.muted ? 0 : current.level);

export const updateVolume = (changes: Partial<VolumeSettings>): void => {
    current = { ...current, ...changes, level: clamp(changes.level ?? current.level) };
    try {
        localStorage.setItem(VOLUME_KEY, JSON.stringify(current));
    } catch {
        // Armazenamento indisponível: o volume vale só até recarregar a página.
    }

    listeners.forEach((listener) => listener());
};

export const subscribeVolume = (listener: () => void): (() => void) => {
    listeners.add(listener);

    return () => {
        listeners.delete(listener);
    };
};
