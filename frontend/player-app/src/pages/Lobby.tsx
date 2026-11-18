import { useMemo, useState } from "react";
import Avatar from "../components/Avatar";
import AvatarPicker from "../components/AvatarPicker";
import ConnectionBanner from "../components/ConnectionBanner";
import MatchSettingsPanel from "../components/MatchSettingsPanel";
import MatchSettingsSummary from "../components/MatchSettingsSummary";
import PlayerList from "../components/PlayerList";
import { usePlayer } from "../hooks/usePlayer";
import type { PlayerSession, RoomState } from "../types/protocol";

interface ILobby {
    room: RoomState;
    session: PlayerSession;
}

export default function Lobby({ room, session }: ILobby) {
    const { leave, updateAvatar, connection } = usePlayer();
    const [confirmLeave, setConfirmLeave] = useState(false);
    const [picking, setPicking] = useState(false);
    const me = room.players.find((player) => player.id === session.playerId);
    const vip = room.players.find((player) => player.owner);
    const taken = useMemo(
        () =>
            new Set(
                room.players
                    .filter((player) => player.id !== session.playerId && player.avatar)
                    .map((player) => player.avatar as string),
            ),
        [room.players, session.playerId],
    );

    return (
        <div className="flex min-h-dvh flex-col">
            <ConnectionBanner status={connection} hostConnected={room.connected} />

            <div className="flex grow flex-col gap-6 px-6 py-8">
                <header className="flex flex-col items-center gap-1 text-center">
                    <span className="text-sm uppercase text-white/60">Você está na sala</span>
                    <span className="font-['Monoton'] text-4xl tracking-widest">{room.code}</span>
                    {me && <span className="text-lg font-semibold">{me.nickname}</span>}
                </header>

                {me && (
                    <section className="flex flex-col items-center gap-3">
                        <Avatar avatar={me.avatar} kind="idle" className="h-40 w-40" alt="Seu personagem" />
                        <button
                            type="button"
                            className="rounded-full bg-white/10 px-4 py-1.5 text-sm font-semibold text-white/80"
                            onClick={() => setPicking((open) => !open)}
                        >
                            {picking ? "Fechar" : "Trocar personagem"}
                        </button>
                        {picking && (
                            <AvatarPicker
                                current={me.avatar}
                                taken={taken}
                                onSelect={(avatar) => {
                                    updateAvatar(avatar);
                                    setPicking(false);
                                }}
                            />
                        )}
                    </section>
                )}

                {me?.owner ? (
                    <section className="flex flex-col gap-4 rounded-xl bg-white/10 p-4">
                        <div className="text-center">
                            <p className="text-lg font-bold text-amber-300">Você é o VIP!</p>
                            <p className="text-sm text-white/70">Escolha a playlist, ajuste a partida e comece.</p>
                        </div>
                        <MatchSettingsPanel settings={room.settings} />
                    </section>
                ) : (
                    <div className="flex flex-col gap-3 rounded-xl bg-white/10 p-4 text-center">
                        <p className="text-white/80">
                            {vip
                                ? `Aguardando ${vip.nickname} (VIP) iniciar a partida…`
                                : "Aguardando a partida começar…"}
                        </p>
                        <MatchSettingsSummary settings={room.settings} />
                    </div>
                )}

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
