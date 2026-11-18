interface IRoundDots {
    round: number;
    totalRounds: number;
}

/** Progresso da partida: uma bolinha por rodada, com as jogadas preenchidas e a atual em destaque. */
const RoundDots = ({ round, totalRounds }: IRoundDots) => (
    <ol className="flex flex-wrap justify-center gap-[0.8vh] rounded-full bg-white/5 px-[1.6vh] py-[0.6vh]">
        {Array.from({ length: totalRounds }, (_, index) => index + 1).map((number) => (
            <li
                key={number}
                className={`flex h-[clamp(1.25rem,4vh,2.5rem)] w-[clamp(1.25rem,4vh,2.5rem)] items-center justify-center rounded-full text-[clamp(0.625rem,1.7vh,1rem)] font-bold ${
                    number === round
                        ? "bg-amber-400 text-black"
                        : number < round
                          ? "bg-[#5b3cc4] text-white"
                          : "border border-white/20 text-white/50"
                }`}
                aria-current={number === round ? "step" : undefined}
            >
                {number}
            </li>
        ))}
    </ol>
);

export default RoundDots;
