import { useState } from "react";
import GameCode from "../components/GameCode";
import JoinQrCode from "../components/JoinQrCode";
import Logo from "../components/Logo";
import PlayerList from "../components/PlayerList";
import { useGame } from "../hooks/useGame";
import type { RoomState } from "../types/protocol";

interface ILobby {
    room: RoomState;
}

export default function Lobby({ room }: ILobby) {
    const { kickPlayer, closeRoom } = useGame();
    const [confirmClose, setConfirmClose] = useState(false);
    const vip = room.players.find((player) => player.owner);

    return (
        <div className="flex min-h-screen w-full flex-col gap-8 p-10">
            <header className="flex items-center justify-between">
                <Logo size="small" />
                <button
                    type="button"
                    className={`rounded-lg px-4 py-2 text-sm ${confirmClose ? "bg-red-500 font-bold" : "text-white/50 hover:text-white"}`}
                    onClick={() => (confirmClose ? closeRoom() : setConfirmClose(true))}
                    onBlur={() => setConfirmClose(false)}
                >
                    {confirmClose ? "Confirmar encerramento" : "Encerrar sala"}
                </button>
            </header>

            <main className="flex grow flex-col items-center gap-10 lg:flex-row lg:items-start">
                <section className="flex flex-col items-center gap-6">
                    <div className="flex flex-col items-center">
                        <span className="text-lg uppercase text-white/60">Código da sala</span>
                        <span className="text-7xl text-white">
                            <GameCode code={room.code} />
                        </span>
                    </div>
                    <JoinQrCode code={room.code} />
                </section>

                <section className="flex w-full grow flex-col gap-4">
                    <div className="flex items-baseline justify-between">
                        <h2 className="text-3xl font-bold text-white">Jogadores ({room.players.length})</h2>
                        <span className="text-lg text-white/60">
                            {vip ? `Aguardando ${vip.nickname} (VIP) iniciar a partida` : "Aguardando jogadores"}
                        </span>
                    </div>
                    <PlayerList players={room.players} onKick={kickPlayer} />
                </section>
            </main>
        </div>
    );
}
