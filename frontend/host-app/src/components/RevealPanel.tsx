import type { RoundEndedPayload } from "../types/protocol";
import { formatPoints, formatSeconds, ordinal } from "../utils/round.utils";

interface IRevealPanel {
    ended: RoundEndedPayload;
    remainingMs: number;
}

const SCOREBOARD_SIZE = 8;

/** Revelação da rodada na TV: resposta certa, os mais rápidos que acertaram e o placar. */
const RevealPanel = ({ ended, remainingMs }: IRevealPanel) => (
    <div className="flex w-full max-w-6xl flex-col items-center gap-10">
        <div className="flex flex-col items-center gap-2 text-center">
            <span className="text-xl uppercase text-white/60">A resposta era</span>
            <span className="text-5xl font-bold text-amber-300">{ended.answer}</span>
        </div>

        <div className="grid w-full gap-8 lg:grid-cols-2">
            <section className="flex flex-col gap-3 rounded-2xl bg-white/10 p-6">
                <h2 className="text-2xl font-bold text-white">Mais rápidos</h2>
                {ended.fastest.length === 0 ? (
                    <p className="text-xl text-white/60">Ninguém acertou essa!</p>
                ) : (
                    <ol className="flex flex-col gap-2">
                        {ended.fastest.map((result, index) => (
                            <li key={result.playerId} className="flex items-center gap-4 text-2xl text-white">
                                <span className="w-10 font-bold text-amber-300">{ordinal(index + 1)}</span>
                                <span className="grow truncate font-semibold">{result.nickname}</span>
                                <span className="text-white/60">{formatSeconds(result.responseTimeMs)}</span>
                                <span className="w-24 text-right font-bold text-emerald-400">
                                    +{formatPoints(result.points)}
                                </span>
                            </li>
                        ))}
                    </ol>
                )}
            </section>

            <section className="flex flex-col gap-3 rounded-2xl bg-white/10 p-6">
                <h2 className="text-2xl font-bold text-white">Placar</h2>
                <ol className="flex flex-col gap-2">
                    {ended.scoreboard.slice(0, SCOREBOARD_SIZE).map((score, index) => (
                        <li key={score.playerId} className="flex items-center gap-4 text-2xl text-white">
                            <span className="w-10 font-bold text-white/60">{ordinal(index + 1)}</span>
                            <span className="grow truncate font-semibold">{score.nickname}</span>
                            <span className="font-bold">{formatPoints(score.score)}</span>
                        </li>
                    ))}
                </ol>
            </section>
        </div>

        <p className="text-xl text-white/60">
            {ended.lastRound ? "Resultado final em instantes…" : `Próxima rodada em ${Math.ceil(remainingMs / 1000)} s`}
        </p>
    </div>
);

export default RevealPanel;
