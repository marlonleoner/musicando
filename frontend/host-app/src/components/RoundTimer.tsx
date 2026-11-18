interface IRoundTimer {
    remainingMs: number;
    totalMs: number;
}

const RADIUS = 44;

const CIRCUMFERENCE = 2 * Math.PI * RADIUS;

/** Cronômetro redondo da rodada: o anel esvazia conforme o tempo acaba. */
const RoundTimer = ({ remainingMs, totalMs }: IRoundTimer) => {
    const fraction = totalMs > 0 ? Math.min(1, remainingMs / totalMs) : 0;

    return (
        <div className="relative h-[clamp(3rem,11vh,7rem)] w-[clamp(3rem,11vh,7rem)]">
            <svg viewBox="0 0 100 100" className="h-full w-full -rotate-90" aria-hidden="true">
                <circle
                    cx="50"
                    cy="50"
                    r={RADIUS}
                    fill="rgba(255,255,255,0.05)"
                    stroke="rgba(255,255,255,0.1)"
                    strokeWidth="7"
                />
                <circle
                    cx="50"
                    cy="50"
                    r={RADIUS}
                    fill="none"
                    className={`transition-[stroke-dashoffset] duration-200 ease-linear ${fraction < 0.25 ? "stroke-red-500" : "stroke-amber-400"}`}
                    strokeWidth="7"
                    strokeLinecap="round"
                    strokeDasharray={CIRCUMFERENCE}
                    strokeDashoffset={CIRCUMFERENCE * (1 - fraction)}
                />
            </svg>
            <span className="absolute inset-0 flex items-center justify-center text-[clamp(1rem,4.2vh,3rem)] font-bold text-white">
                {Math.ceil(remainingMs / 1000)}
            </span>
        </div>
    );
};

export default RoundTimer;
