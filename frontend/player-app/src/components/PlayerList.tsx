import type { Player } from "../types/protocol";

interface IPlayerList {
    players: Player[];
    currentPlayerId: string;
}

const PlayerList = ({ players, currentPlayerId }: IPlayerList) => (
    <ul className="flex flex-col gap-2">
        {players.map((player) => (
            <li
                key={player.id}
                className={`flex items-center gap-2 rounded-xl px-4 py-3 ${
                    player.id === currentPlayerId
                        ? "bg-[#5F3A90]"
                        : player.connected
                          ? "bg-[#44277B]"
                          : "bg-white/10 text-white/50"
                }`}
            >
                <span
                    className={`h-2.5 w-2.5 shrink-0 rounded-full ${player.connected ? "bg-emerald-400" : "bg-white/30"}`}
                />
                <span className="truncate font-semibold">{player.nickname}</span>
                {player.id === currentPlayerId && <span className="text-xs text-white/70">(você)</span>}
                {player.owner && (
                    <span className="ml-auto shrink-0 rounded-md bg-amber-400 px-1.5 text-xs font-bold text-black">
                        VIP
                    </span>
                )}
            </li>
        ))}
    </ul>
);

export default PlayerList;
