import Logo from "../components/Logo";
import type { RoomState } from "../types/protocol";
import { formatPoints, ordinal } from "../utils/round.utils";

interface IFinalRanking {
    room: RoomState;
}

const PODIUM_COLORS = ["text-amber-300", "text-slate-200", "text-orange-400"];

export default function FinalRanking({ room }: IFinalRanking) {
    const ranking = [...room.players].sort((a, b) => b.score - a.score || b.roundsWon - a.roundsWon);
    const vip = room.players.find((player) => player.owner);

    return (
        <div className="flex min-h-screen w-full flex-col items-center gap-10 p-10">
            <Logo size="small" />
            <h1 className="text-5xl font-bold text-white">Fim de jogo!</h1>

            <ol className="flex w-full max-w-3xl flex-col gap-3">
                {ranking.map((player, index) => (
                    <li
                        key={player.id}
                        className={`flex items-center gap-6 rounded-2xl px-6 py-4 text-white ${index === 0 ? "bg-amber-400/20 text-3xl" : "bg-white/10 text-2xl"}`}
                    >
                        <span className={`w-14 font-bold ${PODIUM_COLORS[index] ?? "text-white/60"}`}>
                            {ordinal(index + 1)}
                        </span>
                        <span className="grow truncate font-semibold">{player.nickname}</span>
                        <span className="text-lg text-white/60">{player.roundsWon} acertos</span>
                        <span className="w-32 text-right font-bold">{formatPoints(player.score)}</span>
                    </li>
                ))}
            </ol>

            <p className="text-xl text-white/60">
                {vip ? `Aguardando ${vip.nickname} (VIP) começar uma nova partida…` : "Aguardando uma nova partida…"}
            </p>
        </div>
    );
}
