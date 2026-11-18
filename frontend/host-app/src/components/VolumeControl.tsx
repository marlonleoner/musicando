import { useVolume } from "../hooks/useVolume";
import { updateVolume } from "../utils/volume.utils";

/** Volume usado ao reativar o som quando o slider estava no zero. */
const UNMUTE_LEVEL = 0.5;

/**
 * Botão de som no canto inferior esquerdo: o clique muta ou desmuta, e a barra de volume aparece acima dele ao passar
 * o mouse (ou ao focar pelo teclado).
 */
export default function VolumeControl() {
    const { level, muted } = useVolume();
    const percent = muted ? 0 : Math.round(level * 100);
    const silent = percent === 0;

    const toggle = () =>
        updateVolume(silent ? { muted: false, level: level > 0 ? level : UNMUTE_LEVEL } : { muted: true });

    return (
        <div className="group fixed bottom-6 left-6 z-40 flex flex-col items-center">
            <div className="invisible absolute bottom-full pb-2 opacity-0 transition group-focus-within:visible group-focus-within:opacity-100 group-hover:visible group-hover:opacity-100">
                <div className="flex flex-col items-center gap-2 rounded-full bg-black/50 px-2 py-3 backdrop-blur">
                    <span className="text-xs tabular-nums text-white/80">{percent}%</span>
                    <input
                        type="range"
                        min={0}
                        max={100}
                        step={5}
                        value={percent}
                        aria-label="Volume"
                        className="h-28 w-4 accent-amber-400 [direction:rtl] [writing-mode:vertical-lr]"
                        onChange={(event) => updateVolume({ level: Number(event.target.value) / 100, muted: false })}
                    />
                </div>
            </div>
            <button
                type="button"
                className="flex size-12 items-center justify-center rounded-full bg-black/40 text-white/80 backdrop-blur hover:text-white"
                aria-label={silent ? "Ativar som" : "Silenciar"}
                title={silent ? "Ativar som" : "Silenciar"}
                onClick={toggle}
            >
                <svg viewBox="0 0 24 24" className="size-6" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M11 5 6 9H3v6h3l5 4V5Z" fill="currentColor" strokeLinejoin="round" />
                    {silent ? (
                        <path d="m16 9 5 6m0-6-5 6" strokeLinecap="round" />
                    ) : (
                        <path d="M15.5 8.5a5 5 0 0 1 0 7M18.5 5.5a9 9 0 0 1 0 13" strokeLinecap="round" />
                    )}
                </svg>
            </button>
        </div>
    );
}
