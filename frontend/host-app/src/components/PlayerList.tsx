import { useState } from "react";
import type { Player } from "../types/protocol";

interface IPlayerList {
    players: Player[];
    onKick: (playerId: string) => void;
}

const PlayerList = ({ players, onKick }: IPlayerList) => {
    const [confirming, setConfirming] = useState<string | null>(null);

    if (players.length === 0) {
        return <p className="text-xl text-white/60">Nenhum jogador ainda. Escaneie o QR code para entrar!</p>;
    }

    return (
        <ul className="grid w-full grid-cols-2 gap-3 lg:grid-cols-3">
            {players.map((player) => (
                <li
                    key={player.id}
                    className={`flex items-center justify-between gap-2 rounded-xl px-4 py-3 ${
                        player.connected ? "bg-[#44277B]" : "bg-white/10 text-white/50"
                    }`}
                >
                    <div className="flex min-w-0 items-center gap-2">
                        <span
                            className={`h-2.5 w-2.5 shrink-0 rounded-full ${player.connected ? "bg-emerald-400" : "bg-white/30"}`}
                            title={player.connected ? "Conectado" : "Reconectando…"}
                        />
                        <span className="truncate text-lg font-semibold">{player.nickname}</span>
                        {player.owner && (
                            <span className="shrink-0 rounded-md bg-amber-400 px-1.5 text-xs font-bold text-black">
                                VIP
                            </span>
                        )}
                    </div>
                    {confirming === player.id ? (
                        <button
                            type="button"
                            className="shrink-0 rounded-md bg-red-500 px-2 py-1 text-xs font-bold"
                            onClick={() => {
                                setConfirming(null);
                                onKick(player.id);
                            }}
                            onBlur={() => setConfirming(null)}
                        >
                            Remover?
                        </button>
                    ) : (
                        <button
                            type="button"
                            className="shrink-0 text-white/40 hover:text-red-400"
                            title={`Remover ${player.nickname}`}
                            onClick={() => setConfirming(player.id)}
                        >
                            ✕
                        </button>
                    )}
                </li>
            ))}
        </ul>
    );
};

export default PlayerList;
