import CloseRoomButton from "../components/CloseRoomButton";
import GameCode from "../components/GameCode";
import JoinQrCode from "../components/JoinQrCode";
import Logo from "../components/Logo";
import MatchSettingsCard from "../components/MatchSettingsCard";
import PlayerList from "../components/PlayerList";
import { useGame } from "../hooks/useGame";
import type { RoomState } from "../types/protocol";

interface ILobby {
    room: RoomState;
}

export default function Lobby({ room }: ILobby) {
    const { lobbyAudio, kickPlayer, closeRoom } = useGame();
    const vip = room.players.find((player) => player.owner);

    return (
        // O espaço embaixo fica livre para o QR code (direita) e o controle de volume (esquerda), fixos nos cantos.
        <div className="flex min-h-screen w-full flex-col gap-8 p-10 pb-80">
            <header className="flex items-center justify-between gap-6">
                <Logo size="small" />
                <div className="flex items-center gap-6">
                    <div className="flex flex-col items-end leading-none">
                        <span className="text-sm uppercase text-white/60">Código da sala</span>
                        <span className="text-5xl text-white 2xl:text-6xl">
                            <GameCode code={room.code} />
                        </span>
                    </div>
                    <CloseRoomButton onClose={closeRoom} />
                </div>
            </header>

            <main className="flex w-full flex-col gap-4">
                <MatchSettingsCard
                    settings={room.settings}
                    vipNickname={vip?.nickname}
                    playing={Boolean(lobbyAudio?.length)}
                />
                <div className="flex items-baseline justify-between">
                    <h2 className="text-3xl font-bold text-white">Jogadores ({room.players.length})</h2>
                    <span className="text-lg text-white/60">
                        {vip ? `Aguardando ${vip.nickname} (VIP) iniciar a partida` : "Aguardando jogadores"}
                    </span>
                </div>
                <PlayerList players={room.players} onKick={kickPlayer} />
            </main>

            <aside className="fixed bottom-6 right-6">
                <JoinQrCode code={room.code} />
            </aside>
        </div>
    );
}
