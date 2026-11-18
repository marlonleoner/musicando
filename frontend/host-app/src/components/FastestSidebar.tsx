import type { Player, RoundEndedPayload } from "../types/protocol";
import { formatPoints, formatSeconds, ordinal } from "../utils/round.utils";
import AvatarFrame from "./AvatarFrame";

interface IFastestSidebar {
    ended: RoundEndedPayload;
    /** Jogadores da sala, para mostrar o personagem de cada um. */
    players: Player[];
}

const STAGGER_MS = 120;

/** Os mais rápidos que acertaram a rodada, em cards colados na lateral esquerda da TV. */
const FastestSidebar = ({ ended, players }: IFastestSidebar) => {
    const avatarOf = (playerId: string) => players.find((player) => player.id === playerId)?.avatar ?? null;

    return (
        <aside className="absolute left-0 top-44 z-20 flex w-[clamp(16rem,21vw,26rem)] flex-col gap-[1vh]">
            <h2 className="pl-6 text-[clamp(1rem,2.6vh,1.875rem)] font-bold uppercase text-white/70">Mais rápidos</h2>
            {ended.fastest.length === 0 ? (
                <p className="animate-rise rounded-r-2xl bg-white/10 px-6 py-[1.5vh] text-[clamp(1rem,2.4vh,1.75rem)] text-white/60">
                    Ninguém acertou essa!
                </p>
            ) : (
                <ol className="flex flex-col gap-[1vh]">
                    {ended.fastest.map((result, index) => (
                        <li
                            key={result.playerId}
                            className={`animate-rise flex items-center gap-3 rounded-r-2xl py-[1vh] pl-6 pr-4 ${
                                index === 0 ? "bg-[#44277B]" : "bg-white/10"
                            }`}
                            style={{ animationDelay: `${(index + 1) * STAGGER_MS}ms` }}
                        >
                            <span className="w-[2.2em] text-[clamp(1rem,2.4vh,1.75rem)] font-bold italic text-amber-300">
                                {ordinal(index + 1)}
                            </span>
                            <AvatarFrame
                                avatar={avatarOf(result.playerId)}
                                className="h-[clamp(2.5rem,6vh,4.5rem)] w-[clamp(2.5rem,6vh,4.5rem)]"
                            />
                            <span className="flex min-w-0 grow flex-col leading-tight">
                                <span className="truncate text-[clamp(1rem,2.4vh,1.75rem)] font-bold text-white">
                                    {result.nickname}
                                </span>
                                <span className="text-[clamp(0.875rem,2vh,1.375rem)] text-white/60">
                                    {formatSeconds(result.responseTimeMs)} ·{" "}
                                    <span className="font-bold text-emerald-400">+{formatPoints(result.points)}</span>
                                </span>
                            </span>
                        </li>
                    ))}
                </ol>
            )}
        </aside>
    );
};

export default FastestSidebar;
