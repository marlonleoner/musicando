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
        // A tela tem sempre a altura da janela. O QR code (direita) e o controle de volume (esquerda) ficam fixos nos
        // cantos de baixo; a lista de jogadores deixa espaço livre para os dois.
        <div className="relative flex h-dvh w-full flex-col gap-[2vh] overflow-hidden px-[3vw] pt-[2.5vh]">
            <header className="flex shrink-0 items-center justify-between gap-6">
                <Logo size="small" />
                <div className="flex items-center gap-[1.5vw]">
                    <div className="flex flex-col items-end leading-none">
                        <span className="text-[clamp(0.625rem,1.6vh,0.875rem)] uppercase text-white/60">
                            Código da sala
                        </span>
                        <span className="text-[clamp(1.5rem,7vh,4.5rem)] text-white">
                            <GameCode code={room.code} />
                        </span>
                    </div>
                    <CloseRoomButton onClose={closeRoom} />
                </div>
            </header>

            <main className="flex min-h-0 w-full grow flex-col gap-[1.5vh] pb-[4.5rem]">
                <MatchSettingsCard
                    settings={room.settings}
                    vipNickname={vip?.nickname}
                    playing={Boolean(lobbyAudio?.length)}
                />
                <div className="flex shrink-0 items-baseline justify-between gap-4 pr-[calc(30vh+2rem)]">
                    <h2 className="text-[clamp(1rem,3.6vh,2rem)] font-bold text-white">
                        Jogadores ({room.players.length})
                    </h2>
                    <span className="truncate text-[clamp(0.75rem,2.2vh,1.25rem)] text-white/60">
                        {vip ? `Aguardando ${vip.nickname} (VIP) iniciar a partida` : "Aguardando jogadores"}
                    </span>
                </div>
                <div className="min-h-0 pr-[calc(30vh+2rem)]">
                    <PlayerList players={room.players} onKick={kickPlayer} />
                </div>
            </main>

            <aside className="absolute bottom-[2.5vh] right-[3vw]">
                <JoinQrCode code={room.code} />
            </aside>
        </div>
    );
}
