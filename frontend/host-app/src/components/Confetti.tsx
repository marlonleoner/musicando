const COLORS = ["bg-amber-400", "bg-rose-500", "bg-sky-400", "bg-emerald-400", "bg-fuchsia-500", "bg-white"];

const PIECES = Array.from({ length: 60 }, (_, index) => ({
    left: (index * 37) % 100,
    delay: ((index * 13) % 30) / 10,
    duration: 3 + (index % 5) * 0.6,
    color: COLORS[index % COLORS.length],
    wide: index % 3 === 0,
}));

/** Confete caindo pela tela, sem bloquear cliques. */
const Confetti = () => (
    <div className="pointer-events-none fixed inset-0 overflow-hidden" aria-hidden="true">
        {PIECES.map((piece, index) => (
            <span
                key={index}
                className={`animate-confetti absolute -top-8 rounded-sm ${piece.color} ${piece.wide ? "h-2 w-4" : "h-4 w-2"}`}
                style={{
                    left: `${piece.left}%`,
                    animationDelay: `${piece.delay}s`,
                    animationDuration: `${piece.duration}s`,
                }}
            />
        ))}
    </div>
);

export default Confetti;
