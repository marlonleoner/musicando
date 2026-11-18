interface IRoundDots {
    round: number;
    totalRounds: number;
}

/** Progresso da partida: uma bolinha por rodada, com as jogadas preenchidas e a atual em destaque. */
const RoundDots = ({ round, totalRounds }: IRoundDots) => (
    <ol className="flex flex-wrap justify-center gap-2 rounded-full bg-white/5 px-4 py-2">
        {Array.from({ length: totalRounds }, (_, index) => index + 1).map((number) => (
            <li
                key={number}
                className={`flex h-8 w-8 items-center justify-center rounded-full text-sm font-bold 2xl:h-10 2xl:w-10 2xl:text-base ${
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
