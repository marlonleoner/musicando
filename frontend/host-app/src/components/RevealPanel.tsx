import type { Player, RoundEndedPayload, Score } from "../types/protocol";
import { formatPoints, formatSeconds, ordinal, positionChange } from "../utils/round.utils";
import Avatar from "./Avatar";
import PositionChange from "./PositionChange";

interface IRevealPanel {
    ended: RoundEndedPayload;
    previousScoreboard: Score[];
    remainingMs: number;
    /** Jogadores da sala, para mostrar o personagem de cada um. */
    players: Player[];
}

const SCOREBOARD_SIZE = 8;

const STAGGER_MS = 120;

/** Revelação da rodada na TV: capa, resposta certa, os mais rápidos que acertaram e o placar. */
const RevealPanel = ({ ended, previousScoreboard, remainingMs, players }: IRevealPanel) => {
    const avatarOf = (playerId: string) => players.find((player) => player.id === playerId)?.avatar ?? null;

    return (
        <div className="flex w-full max-w-6xl flex-col items-center gap-10 2xl:max-w-[1600px]">
            <div className="flex items-center gap-8">
                {ended.cover && (
                    <img
                        src={ended.cover}
                        alt=""
                        className="animate-pop aspect-square w-40 rounded-2xl object-cover shadow-2xl shadow-black/50 2xl:w-64"
                    />
                )}
                <div className="flex flex-col gap-2">
                    <span className="animate-rise text-xl uppercase text-white/60 2xl:text-3xl">A resposta era</span>
                    <span className="animate-pop text-5xl font-bold text-amber-300 2xl:text-7xl">{ended.answer}</span>
                    {ended.artist && (
                        <span
                            className="animate-rise text-2xl text-white/80 2xl:text-4xl"
                            style={{ animationDelay: "200ms" }}
                        >
                            {ended.artist}
                        </span>
                    )}
                </div>
            </div>

            <div className="grid w-full gap-8 lg:grid-cols-2">
                <section className="flex flex-col gap-3 rounded-2xl bg-white/10 p-6">
                    <h2 className="text-2xl font-bold text-white 2xl:text-4xl">Mais rápidos</h2>
                    {ended.fastest.length === 0 ? (
                        <p className="text-xl text-white/60 2xl:text-3xl">Ninguém acertou essa!</p>
                    ) : (
                        <ol className="flex flex-col gap-2">
                            {ended.fastest.map((result, index) => (
                                <li
                                    key={result.playerId}
                                    className="animate-rise flex items-center gap-4 text-2xl text-white 2xl:text-4xl"
                                    style={{ animationDelay: `${(index + 1) * STAGGER_MS}ms` }}
                                >
                                    <span className="w-10 font-bold text-amber-300 2xl:w-16">{ordinal(index + 1)}</span>
                                    <Avatar
                                        avatar={avatarOf(result.playerId)}
                                        kind="cheer"
                                        className="animate-hop h-14 w-14 shrink-0 2xl:h-20 2xl:w-20"
                                    />
                                    <span className="grow truncate font-semibold">{result.nickname}</span>
                                    <span className="text-white/60">{formatSeconds(result.responseTimeMs)}</span>
                                    <span className="w-24 text-right font-bold text-emerald-400 2xl:w-36">
                                        +{formatPoints(result.points)}
                                    </span>
                                </li>
                            ))}
                        </ol>
                    )}
                </section>

                <section className="flex flex-col gap-3 rounded-2xl bg-white/10 p-6">
                    <h2 className="text-2xl font-bold text-white 2xl:text-4xl">Placar</h2>
                    <ol className="flex flex-col gap-2">
                        {ended.scoreboard.slice(0, SCOREBOARD_SIZE).map((score, index) => (
                            <li
                                key={score.playerId}
                                className="animate-rise flex items-center gap-4 text-2xl text-white 2xl:text-4xl"
                                style={{ animationDelay: `${(index + 1) * STAGGER_MS}ms` }}
                            >
                                <span className="w-10 font-bold text-white/60 2xl:w-16">{ordinal(index + 1)}</span>
                                <Avatar
                                    avatar={avatarOf(score.playerId)}
                                    kind="face"
                                    className="h-10 w-10 shrink-0 2xl:h-14 2xl:w-14"
                                />
                                <span className="grow truncate font-semibold">{score.nickname}</span>
                                {previousScoreboard.length > 0 && (
                                    <PositionChange
                                        change={positionChange(previousScoreboard, ended.scoreboard, score.playerId)}
                                        className="text-lg 2xl:text-2xl"
                                    />
                                )}
                                <span className="font-bold">{formatPoints(score.score)}</span>
                            </li>
                        ))}
                    </ol>
                </section>
            </div>

            <p className="text-xl text-white/60 2xl:text-3xl">
                {ended.lastRound
                    ? "Resultado final em instantes…"
                    : `Próxima rodada em ${Math.ceil(remainingMs / 1000)} s`}
            </p>
        </div>
    );
};

export default RevealPanel;
