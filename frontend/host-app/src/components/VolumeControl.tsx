import { useVolume } from "../hooks/useVolume";
import { updateVolume } from "../utils/volume.utils";

/** Volume usado ao reativar o som quando o slider estava no zero. */
const UNMUTE_LEVEL = 0.5;

export default function VolumeControl() {
    const { level, muted } = useVolume();
    const percent = muted ? 0 : Math.round(level * 100);
    const silent = percent === 0;

    const toggle = () =>
        updateVolume(silent ? { muted: false, level: level > 0 ? level : UNMUTE_LEVEL } : { muted: true });

    return (
        <div className="fixed bottom-6 right-6 z-40 flex items-center gap-3 rounded-full bg-black/40 px-4 py-2 text-white/80 backdrop-blur">
            <button
                type="button"
                className="hover:text-white"
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
            <input
                type="range"
                min={0}
                max={100}
                step={5}
                value={percent}
                aria-label="Volume"
                className="w-32 accent-amber-400"
                onChange={(event) => updateVolume({ level: Number(event.target.value) / 100, muted: false })}
            />
            <span className="w-10 text-right text-sm tabular-nums">{percent}%</span>
        </div>
    );
}
