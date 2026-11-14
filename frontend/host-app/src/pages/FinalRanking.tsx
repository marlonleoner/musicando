import { useEffect } from "react";
import Confetti from "../components/Confetti";
import Logo from "../components/Logo";
import type { Player, RoomState } from "../types/protocol";
import { formatPoints, ordinal } from "../utils/round.utils";
import { playFanfare } from "../utils/sound.utils";

interface IFinalRanking {
    room: RoomState;
}

/** Ordem de exibição do pódio (2º, 1º, 3º), a altura de cada degrau e quando ele aparece. */
const PODIUM = [
    { position: 1, height: "h-40 2xl:h-56", color: "bg-slate-300 text-slate-900", delay: 400 },
    { position: 0, height: "h-56 2xl:h-80", color: "bg-amber-400 text-black", delay: 800 },
    { position: 2, height: "h-28 2xl:h-40", color: "bg-orange-400 text-black", delay: 0 },
];

export default function FinalRanking({ room }: IFinalRanking) {
    const ranking = [...room.players].sort((a, b) => b.score - a.score || b.roundsWon - a.roundsWon);
    const rest = ranking.slice(PODIUM.length);
    const vip = room.players.find((player) => player.owner);

    useEffect(() => {
        playFanfare();
    }, []);

    return (
        <div className="relative flex min-h-screen w-full flex-col items-center gap-10 p-10">
            <Confetti />
            <Logo size="small" />
            <h1 className="animate-pop text-5xl font-bold text-white 2xl:text-7xl">Fim de jogo!</h1>

            <div className="flex items-end justify-center gap-4 2xl:gap-8">
                {PODIUM.map(({ position, height, color, delay }) => {
                    const player: Player | undefined = ranking[position];
                    if (!player) {
                        return null;
                    }

                    return (
                        <div
                            key={player.id}
                            className="animate-rise flex w-48 flex-col items-center gap-2 2xl:w-72"
                            style={{ animationDelay: `${delay}ms` }}
                        >
                            <span className="max-w-full truncate text-2xl font-bold text-white 2xl:text-4xl">
                                {player.nickname}
                            </span>
                            <span className="text-xl text-white/80 2xl:text-3xl">{formatPoints(player.score)} pts</span>
                            <div
                                className={`flex w-full items-start justify-center rounded-t-2xl pt-4 text-5xl font-bold 2xl:text-7xl ${height} ${color}`}
                            >
                                {ordinal(position + 1)}
                            </div>
                        </div>
                    );
                })}
            </div>

            {rest.length > 0 && (
                <ol className="flex w-full max-w-3xl flex-col gap-3 2xl:max-w-5xl">
                    {rest.map((player, index) => (
                        <li
                            key={player.id}
                            className="animate-rise flex items-center gap-6 rounded-2xl bg-white/10 px-6 py-3 text-2xl text-white 2xl:text-3xl"
                            style={{ animationDelay: `${1200 + index * 100}ms` }}
                        >
                            <span className="w-14 font-bold text-white/60">{ordinal(index + PODIUM.length + 1)}</span>
                            <span className="grow truncate font-semibold">{player.nickname}</span>
                            <span className="text-lg text-white/60 2xl:text-2xl">{player.roundsWon} acertos</span>
                            <span className="w-32 text-right font-bold">{formatPoints(player.score)}</span>
                        </li>
                    ))}
                </ol>
            )}

            <p className="text-xl text-white/60 2xl:text-3xl">
                {vip ? `Aguardando ${vip.nickname} (VIP) começar uma nova partida…` : "Aguardando uma nova partida…"}
            </p>
        </div>
    );
}
