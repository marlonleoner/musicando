import type { MatchSettings } from "../types/protocol";

interface IMatchSettingsCard {
    settings: MatchSettings | null;
    vipNickname?: string;
}

/** Configuração atual da partida, atualizada a cada escolha do VIP. */
const MatchSettingsCard = ({ settings, vipNickname }: IMatchSettingsCard) => {
    if (!settings?.playlist) {
        return (
            <div className="rounded-2xl bg-white/10 p-6 text-xl text-white/60">
                {vipNickname
                    ? `Aguardando ${vipNickname} (VIP) escolher a playlist…`
                    : "Aguardando o VIP escolher a playlist…"}
            </div>
        );
    }

    const { playlist } = settings;

    return (
        <div className="flex items-center gap-6 rounded-2xl bg-white/10 p-6">
            {playlist.cover ? (
                <img
                    src={playlist.cover}
                    alt={playlist.name}
                    className="aspect-square w-32 shrink-0 rounded-xl object-cover"
                />
            ) : (
                <div className="flex aspect-square w-32 shrink-0 items-center justify-center rounded-xl bg-white/10 text-5xl">
                    ♪
                </div>
            )}
            <div className="flex min-w-0 flex-col gap-1">
                <span className="text-sm uppercase text-white/60">Playlist</span>
                <span className="truncate text-3xl font-bold text-white">{playlist.name}</span>
                <span className="text-xl text-white/80">
                    {settings.rounds} rodadas · {settings.roundDuration} s por rodada
                </span>
            </div>
        </div>
    );
};

export default MatchSettingsCard;
