import type { MatchSettings } from "../types/protocol";
import Equalizer from "./Equalizer";

interface IMatchSettingsCard {
    settings: MatchSettings | null;
    vipNickname?: string;
    /** A música do lobby está tocando trechos desta playlist. */
    playing?: boolean;
}

/** Configuração atual da partida, atualizada a cada escolha do VIP. */
const MatchSettingsCard = ({ settings, vipNickname, playing = false }: IMatchSettingsCard) => {
    if (!settings?.playlist) {
        return (
            <div className="rounded-2xl bg-white/10 p-[2vh] text-[clamp(0.875rem,2.8vh,1.5rem)] text-white/60">
                {vipNickname
                    ? `Aguardando ${vipNickname} (VIP) escolher a playlist…`
                    : "Aguardando o VIP escolher a playlist…"}
            </div>
        );
    }

    const { playlist } = settings;

    return (
        <div className="flex items-center gap-[2vw] rounded-2xl bg-white/10 p-[2vh]">
            {playlist.cover ? (
                <img
                    src={playlist.cover}
                    alt={playlist.name}
                    className="aspect-square w-[clamp(3rem,13vh,8rem)] shrink-0 rounded-xl object-cover"
                />
            ) : (
                <div className="flex aspect-square w-[clamp(3rem,13vh,8rem)] shrink-0 items-center justify-center rounded-xl bg-white/10 text-[clamp(1.5rem,6vh,3rem)]">
                    ♪
                </div>
            )}
            <div className="flex min-w-0 grow flex-col gap-[0.5vh]">
                <span className="text-[clamp(0.625rem,1.6vh,0.875rem)] uppercase text-white/60">Playlist</span>
                <span className="truncate text-[clamp(1rem,4vh,2.25rem)] font-bold text-white">{playlist.name}</span>
                <span className="text-[clamp(0.75rem,2.6vh,1.5rem)] text-white/80">
                    {settings.rounds} rodadas · {settings.roundDuration} s por rodada
                </span>
            </div>
            {playing && (
                <div className="shrink-0" title="Tocando trechos da playlist">
                    <Equalizer compact />
                </div>
            )}
        </div>
    );
};

export default MatchSettingsCard;
