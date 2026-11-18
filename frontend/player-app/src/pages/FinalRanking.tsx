import { useState } from "react";
import ConnectionBanner from "../components/ConnectionBanner";
import { usePlayer } from "../hooks/usePlayer";
import type { PlayerSession, RoomState } from "../types/protocol";
import { formatPoints, ordinal } from "../utils/round.utils";

interface IFinalRanking {
    room: RoomState;
    session: PlayerSession;
}

export default function FinalRanking({ room, session }: IFinalRanking) {
    const { playAgain, leave, connection } = usePlayer();
    const [confirmLeave, setConfirmLeave] = useState(false);
    const ranking = [...room.players].sort((a, b) => b.score - a.score || b.roundsWon - a.roundsWon);
    const position = ranking.findIndex((player) => player.id === session.playerId);
    const me = ranking[position];
    const vip = room.players.find((player) => player.owner);

    return (
        <div className="flex min-h-dvh flex-col">
            <ConnectionBanner status={connection} hostConnected={room.connected} />

            <div className="flex grow flex-col gap-6 px-6 py-8">
                <header className="flex flex-col items-center gap-1 text-center">
                    <span className="text-sm uppercase text-white/60">Fim de jogo</span>
                    {me && (
                        <>
                            <span className="animate-pop text-6xl font-bold text-amber-300">
                                {ordinal(position + 1)}
                            </span>
                            <span className="text-lg">
                                {formatPoints(me.score)} pontos · {me.roundsWon} acertos
                            </span>
                        </>
                    )}
                </header>

                <ol className="flex flex-col gap-2">
                    {ranking.map((player, index) => (
                        <li
                            key={player.id}
                            className={`flex items-center gap-3 rounded-xl px-4 py-3 ${player.id === session.playerId ? "bg-[#5F3A90]" : "bg-white/10"}`}
                        >
                            <span className="w-8 font-bold text-white/60">{ordinal(index + 1)}</span>
                            <span className="grow truncate font-semibold">{player.nickname}</span>
                            <span className="font-bold">{formatPoints(player.score)}</span>
                        </li>
                    ))}
                </ol>

                <div className="mt-auto flex flex-col gap-3">
                    {me?.owner ? (
                        <button
                            type="button"
                            disabled={connection !== "connected"}
                            className="rounded-xl bg-emerald-500 py-3 text-lg font-bold disabled:opacity-40"
                            onClick={playAgain}
                        >
                            Jogar novamente
                        </button>
                    ) : (
                        <p className="text-center text-white/70">
                            {vip
                                ? `Aguardando ${vip.nickname} (VIP) começar outra partida…`
                                : "Aguardando outra partida…"}
                        </p>
                    )}
                    <button
                        type="button"
                        className={`rounded-xl py-3 font-semibold ${confirmLeave ? "bg-red-500" : "bg-white/10 text-white/70"}`}
                        onClick={() => (confirmLeave ? leave() : setConfirmLeave(true))}
                        onBlur={() => setConfirmLeave(false)}
                    >
                        {confirmLeave ? "Confirmar saída" : "Sair da sala"}
                    </button>
                </div>
            </div>
        </div>
    );
}
