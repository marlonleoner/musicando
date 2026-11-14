interface ITimeBar {
    remainingMs: number;
    totalMs: number;
    className?: string;
}

/** Barra que esvazia conforme o tempo da etapa acaba. */
const TimeBar = ({ remainingMs, totalMs, className = "" }: ITimeBar) => {
    const percent = totalMs > 0 ? Math.min(100, (remainingMs / totalMs) * 100) : 0;

    return (
        <div className={`h-3 w-full overflow-hidden rounded-full bg-white/10 ${className}`}>
            <div
                className={`h-full rounded-full transition-[width] duration-200 ease-linear ${percent < 25 ? "bg-red-500" : "bg-amber-400"}`}
                style={{ width: `${percent}%` }}
            />
        </div>
    );
};

export default TimeBar;
