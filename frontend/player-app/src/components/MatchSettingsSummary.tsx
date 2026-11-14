import type { MatchSettings } from "../types/protocol";
import PlaylistCover from "./PlaylistCover";

interface IMatchSettingsSummary {
    settings: MatchSettings | null;
}

/** Configuração da partida, somente leitura, para quem não é o VIP. */
const MatchSettingsSummary = ({ settings }: IMatchSettingsSummary) => {
    if (!settings?.playlist) {
        return <p className="text-sm text-white/60">O VIP ainda está escolhendo a playlist.</p>;
    }

    return (
        <div className="flex items-center gap-3 text-left">
            <PlaylistCover cover={settings.playlist.cover} name={settings.playlist.name} className="w-16 shrink-0" />
            <div className="flex min-w-0 flex-col">
                <span className="truncate font-semibold">{settings.playlist.name}</span>
                <span className="text-sm text-white/70">
                    {settings.rounds} rodadas · {settings.roundDuration} s por rodada
                </span>
            </div>
        </div>
    );
};

export default MatchSettingsSummary;
