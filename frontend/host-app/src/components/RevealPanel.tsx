import type { Player, RoundEndedPayload } from "../types/protocol";
import { OPTION_STYLES } from "../utils/option.utils";
import AvatarFrame from "./AvatarFrame";

interface IRevealPanel {
    ended: RoundEndedPayload;
    /** Jogadores da sala, para mostrar o personagem de quem marcou cada opção. */
    players: Player[];
}

/**
 * Revelação da rodada na TV: capa, resposta certa e artista, e as quatro opções com quem marcou cada uma (a certa em
 * destaque). Os mais rápidos ficam na lateral esquerda ({@link FastestSidebar}) e o placar no rodapé.
 */
const RevealPanel = ({ ended, players }: IRevealPanel) => {
    const avatarOf = (playerId: string) => players.find((player) => player.id === playerId)?.avatar ?? null;

    return (
        <div className="flex w-full flex-col items-center gap-[2.5vh] px-[clamp(11.5rem,23vw,28rem)]">
            <div className="flex items-center gap-[3vh]">
                {ended.cover && (
                    <img
                        src={ended.cover}
                        alt=""
                        className="animate-pop aspect-square w-[clamp(5rem,17vh,14rem)] shrink-0 rounded-2xl object-cover shadow-2xl shadow-black/50"
                    />
                )}
                <div className="flex flex-col gap-[0.8vh]">
                    <span className="animate-rise text-[clamp(0.875rem,2.2vh,1.75rem)] uppercase text-white/60">
                        A resposta era
                    </span>
                    <span className="animate-pop text-balance text-[clamp(1.5rem,5.5vh,4.5rem)] font-bold leading-tight text-amber-300">
                        {ended.answer}
                    </span>
                    {ended.artist && (
                        <span
                            className="animate-rise text-[clamp(1rem,3vh,2.25rem)] text-white/80"
                            style={{ animationDelay: "200ms" }}
                        >
                            {ended.artist}
                        </span>
                    )}
                </div>
            </div>

            {ended.options.length > 0 && (
                <ul className="grid w-full auto-rows-fr grid-cols-2 gap-[1.2vh]">
                    {ended.options.map((option, index) => {
                        const correct = option === ended.answer;
                        const pickers = ended.results.filter((result) => result.option === index);

                        return (
                            <li
                                key={option}
                                className={`animate-rise flex items-center gap-[1vw] rounded-2xl border border-l-8 px-[1.2vw] py-[1vh] ${
                                    correct
                                        ? "border-emerald-400 bg-emerald-500/20"
                                        : `border-white/10 bg-white/5 opacity-60 ${OPTION_STYLES[index]?.border ?? ""}`
                                }`}
                                style={{ animationDelay: `${300 + index * 80}ms` }}
                            >
                                <span
                                    className={`flex h-[clamp(1.25rem,4vh,3rem)] w-[clamp(1.25rem,4vh,3rem)] shrink-0 items-center justify-center rounded-lg text-[clamp(0.75rem,2.2vh,1.5rem)] font-bold text-white ${
                                        OPTION_STYLES[index]?.badge ?? "bg-white/20"
                                    }`}
                                >
                                    {correct ? "✓" : OPTION_STYLES[index]?.letter}
                                </span>
                                <span className="min-w-0 grow text-[clamp(0.75rem,2.4vh,1.75rem)] font-bold leading-tight text-white">
                                    {option}
                                </span>
                                {/* Rostos de quem marcou esta opção. */}
                                <span className="flex shrink-0 -space-x-[1vh]">
                                    {pickers.map((result) => (
                                        <AvatarFrame
                                            key={result.playerId}
                                            avatar={avatarOf(result.playerId)}
                                            className="h-[clamp(1.25rem,4.5vh,3.5rem)] w-[clamp(1.25rem,4.5vh,3.5rem)]"
                                        />
                                    ))}
                                </span>
                            </li>
                        );
                    })}
                </ul>
            )}

            {/* O tempo até a próxima rodada aparece na barra do letreiro. */}
            {ended.lastRound && (
                <p className="text-[clamp(1rem,2.4vh,1.875rem)] text-white/60">Resultado final em instantes…</p>
            )}
        </div>
    );
};

export default RevealPanel;
