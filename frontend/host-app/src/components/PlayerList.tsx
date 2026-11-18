import { useState } from "react";
import type { Player } from "../types/protocol";
import Avatar from "./Avatar";

interface IPlayerList {
    players: Player[];
    onKick: (playerId: string) => void;
}

/** Jogadores do lobby em cards de corpo inteiro, com o personagem animado. */
const PlayerList = ({ players, onKick }: IPlayerList) => {
    const [confirming, setConfirming] = useState<string | null>(null);

    if (players.length === 0) {
        return <p className="text-xl text-white/60">Nenhum jogador ainda. Escaneie o QR code para entrar!</p>;
    }

    return (
        <ul className="grid w-full grid-cols-3 gap-3 md:grid-cols-4 xl:grid-cols-6">
            {players.map((player) => (
                <li
                    key={player.id}
                    className={`animate-pop relative flex flex-col items-center gap-1 rounded-2xl px-2 pb-3 pt-2 ${
                        player.connected ? (player.owner ? "bg-[#5b3cc4]" : "bg-[#44277B]") : "bg-white/10 opacity-50"
                    }`}
                >
                    <Avatar avatar={player.avatar} kind="idle" className="h-32 w-32 2xl:h-44 2xl:w-44" />
                    <span className="max-w-full truncate text-lg font-semibold text-white 2xl:text-2xl">
                        {player.nickname}
                    </span>
                    {player.owner ? (
                        <span className="rounded-md bg-amber-400 px-1.5 text-xs font-bold text-black">VIP</span>
                    ) : (
                        <span className="text-xs text-white/60">{player.connected ? "Pronto" : "Reconectando…"}</span>
                    )}
                    {confirming === player.id ? (
                        <button
                            type="button"
                            className="absolute right-2 top-2 rounded-md bg-red-500 px-2 py-1 text-xs font-bold"
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
                            className="absolute right-2 top-2 text-white/30 hover:text-red-400"
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
