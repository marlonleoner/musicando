import { useEffect } from "react";
import AvatarFrame from "../components/AvatarFrame";
import Avatar from "../components/Avatar";
import Confetti from "../components/Confetti";
import Logo from "../components/Logo";
import type { Player, RoomState } from "../types/protocol";
import { formatPoints, ordinal } from "../utils/round.utils";
import { playFanfare } from "../utils/sound.utils";

interface IFinalRanking {
    room: RoomState;
}

/** Ordem de exibição do pódio dos 5 melhores (4º, 2º, 1º, 3º, 5º), a altura de cada degrau e quando ele aparece. */
const PODIUM = [
    { position: 3, height: "h-[10vh]", color: "bg-[#7f6ad8] text-white", delay: 0 },
    { position: 1, height: "h-[20vh]", color: "bg-slate-300 text-slate-900", delay: 600 },
    { position: 0, height: "h-[28vh]", color: "bg-amber-400 text-black", delay: 900 },
    { position: 2, height: "h-[15vh]", color: "bg-orange-400 text-black", delay: 300 },
    { position: 4, height: "h-[7vh]", color: "bg-[#5b3cc4] text-white", delay: 0 },
];

/**
 * Resultado final na TV. A tela tem sempre a altura da janela: o título fica no topo, o pódio dos 5 melhores colado
 * no rodapé e os demais jogadores numa lista colada na lateral direita.
 */
export default function FinalRanking({ room }: IFinalRanking) {
    const ranking = [...room.players].sort((a, b) => b.score - a.score || b.roundsWon - a.roundsWon);
    const rest = ranking.slice(PODIUM.length);
    const vip = room.players.find((player) => player.owner);

    useEffect(() => {
        playFanfare();
    }, []);

    return (
        <div className="relative flex h-dvh w-full flex-col items-center overflow-hidden">
            <Confetti />
            <header className="flex shrink-0 flex-col items-center gap-[1vh] pt-[2vh]">
                <Logo size="small" />
                <h1 className="animate-pop text-[clamp(1.5rem,6vh,4.5rem)] font-bold leading-none text-white">
                    Fim de jogo!
                </h1>
            </header>

            {/* Espaço livre entre o título e o pódio. */}
            <div className="min-h-0 grow" />

            {rest.length > 0 && (
                <aside className="absolute right-0 top-[18vh] z-10 flex w-[clamp(11rem,19vw,24rem)] flex-col gap-[0.8vh]">
                    <h2 className="pr-[1.2vw] text-right text-[clamp(0.75rem,2.6vh,1.875rem)] font-bold uppercase text-white/70">
                        Demais jogadores
                    </h2>
                    <ol className="flex flex-col gap-[0.8vh]">
                        {rest.map((player, index) => (
                            <li
                                key={player.id}
                                className="animate-rise flex items-center gap-[0.8vw] rounded-l-2xl bg-white/10 py-[0.6vh] pl-[0.8vw] pr-[1.2vw]"
                                style={{ animationDelay: `${1200 + index * 100}ms` }}
                            >
                                <span className="w-[2.4em] shrink-0 text-[clamp(0.75rem,2.4vh,1.75rem)] font-bold italic text-white/60">
                                    {ordinal(index + PODIUM.length + 1)}
                                </span>
                                <AvatarFrame
                                    avatar={player.avatar}
                                    className="h-[clamp(1.5rem,4.5vh,4rem)] w-[clamp(1.5rem,4.5vh,4rem)]"
                                />
                                <span className="flex min-w-0 grow flex-col leading-tight">
                                    <span className="truncate text-[clamp(0.75rem,2.4vh,1.75rem)] font-bold text-white">
                                        {player.nickname}
                                    </span>
                                    <span className="whitespace-nowrap text-[clamp(0.625rem,2vh,1.375rem)] text-white/60">
                                        {formatPoints(player.score)} pts
                                    </span>
                                </span>
                            </li>
                        ))}
                    </ol>
                </aside>
            )}

            {/* Pódio sempre centralizado no rodapé; é estreito o bastante para não encostar na lista lateral. */}
            <div className="flex w-full shrink-0 items-end justify-center gap-[1vw] px-[5rem]">
                {PODIUM.map(({ position, height, color, delay }) => {
                    const player: Player | undefined = ranking[position];
                    if (!player) {
                        return null;
                    }

                    return (
                        <div
                            key={player.id}
                            className="animate-rise flex w-[clamp(5rem,11vw,14rem)] flex-col items-center gap-[0.5vh]"
                            style={{ animationDelay: `${delay}ms` }}
                        >
                            <Avatar
                                avatar={player.avatar}
                                kind="cheer"
                                className="animate-hop h-[clamp(3rem,14vh,11rem)] w-[clamp(3rem,14vh,11rem)]"
                                alt=""
                            />
                            <span className="max-w-full truncate text-[clamp(0.875rem,3vh,2.25rem)] font-bold text-white">
                                {player.nickname}
                            </span>
                            <span className="text-[clamp(0.75rem,2.4vh,1.75rem)] text-white/80">
                                {formatPoints(player.score)} pts
                            </span>
                            <div
                                className={`flex w-full items-start justify-center rounded-t-2xl pt-[1.5vh] text-[clamp(1.25rem,5vh,4rem)] font-bold leading-none ${height} ${color}`}
                            >
                                {ordinal(position + 1)}
                            </div>
                        </div>
                    );
                })}
            </div>

            <p className="absolute right-[2vw] top-[2vh] max-w-[20vw] text-right text-[clamp(0.75rem,2vh,1.25rem)] text-white/60">
                {vip ? `Aguardando ${vip.nickname} (VIP) começar uma nova partida…` : "Aguardando uma nova partida…"}
            </p>
        </div>
    );
}
