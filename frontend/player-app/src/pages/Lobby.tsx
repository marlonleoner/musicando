import { useState } from "react";
import ConnectionBanner from "../components/ConnectionBanner";
import PlayerList from "../components/PlayerList";
import { usePlayer } from "../hooks/usePlayer";
import type { PlayerSession, RoomState } from "../types/protocol";

interface ILobby {
    room: RoomState;
    session: PlayerSession;
}

export default function Lobby({ room, session }: ILobby) {
    const { leave, connection } = usePlayer();
    const [confirmLeave, setConfirmLeave] = useState(false);
    const me = room.players.find((player) => player.id === session.playerId);
    const vip = room.players.find((player) => player.owner);

    return (
        <div className="flex min-h-screen flex-col">
            <ConnectionBanner status={connection} hostConnected={room.connected} />

            <div className="flex grow flex-col gap-6 px-6 py-8">
                <header className="flex flex-col items-center gap-1 text-center">
                    <span className="text-sm uppercase text-white/60">Você está na sala</span>
                    <span className="font-['Monoton'] text-4xl tracking-widest">{room.code}</span>
                    {me && <span className="text-lg font-semibold">{me.nickname}</span>}
                </header>

                <div className="rounded-xl bg-white/10 p-4 text-center">
                    {me?.owner ? (
                        <>
                            <p className="text-lg font-bold text-amber-300">Você é o VIP!</p>
                            <p className="text-sm text-white/70">
                                Em breve você poderá escolher a playlist e iniciar a partida por aqui.
                            </p>
                        </>
                    ) : (
                        <p className="text-white/80">
                            {vip
                                ? `Aguardando ${vip.nickname} (VIP) iniciar a partida…`
                                : "Aguardando a partida começar…"}
                        </p>
                    )}
                </div>

                <section className="flex flex-col gap-3">
                    <h2 className="text-sm uppercase text-white/60">Jogadores ({room.players.length})</h2>
                    <PlayerList players={room.players} currentPlayerId={session.playerId} />
                </section>

                <button
                    type="button"
                    className={`mt-auto rounded-xl py-3 font-semibold ${confirmLeave ? "bg-red-500" : "bg-white/10 text-white/70"}`}
                    onClick={() => (confirmLeave ? leave() : setConfirmLeave(true))}
                    onBlur={() => setConfirmLeave(false)}
                >
                    {confirmLeave ? "Confirmar saída" : "Sair da sala"}
                </button>
            </div>
        </div>
    );
}
