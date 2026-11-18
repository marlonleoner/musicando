const BARS = [0, 150, 300, 450, 600];

interface IEqualizer {
    /** Versão pequena, para caber em um card. */
    compact?: boolean;
}

/** Barras animadas indicando que a música está tocando. */
const Equalizer = ({ compact = false }: IEqualizer) => (
    <div className={`flex items-end ${compact ? "h-10 gap-1" : "h-24 gap-2"}`} aria-hidden="true">
        {BARS.map((delay) => (
            <span
                key={delay}
                className={`${compact ? "w-1.5" : "w-4"} animate-bounce rounded-t bg-amber-400`}
                style={{ height: `${40 + (delay % 400) / 8}%`, animationDelay: `${delay}ms` }}
            />
        ))}
    </div>
);

export default Equalizer;
