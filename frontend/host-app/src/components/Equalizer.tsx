const BARS = [0, 150, 300, 450, 600];

/** Barras animadas indicando que a música está tocando. */
const Equalizer = () => (
    <div className="flex h-24 items-end gap-2" aria-hidden="true">
        {BARS.map((delay) => (
            <span
                key={delay}
                className="w-4 animate-bounce rounded-t bg-amber-400"
                style={{ height: `${40 + (delay % 400) / 8}%`, animationDelay: `${delay}ms` }}
            />
        ))}
    </div>
);

export default Equalizer;
