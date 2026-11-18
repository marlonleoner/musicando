import type { RoundEndedPayload } from "../types/protocol";

interface IRevealPanel {
    ended: RoundEndedPayload;
}

/**
 * Revelação da rodada na TV: capa, resposta certa e artista. Os mais rápidos ficam na lateral esquerda
 * ({@link FastestSidebar}) e o placar no rodapé. As laterais da revelação ficam livres para a lista dos mais rápidos,
 * sem tirar a resposta do centro da tela.
 */
const RevealPanel = ({ ended }: IRevealPanel) => (
    <div className="flex w-full flex-col items-center gap-[2.5vh] px-[clamp(11.5rem,23vw,28rem)]">
        <div className="flex items-center gap-[3vh]">
            {ended.cover && (
                <img
                    src={ended.cover}
                    alt=""
                    className="animate-pop aspect-square w-[clamp(7rem,24vh,18rem)] shrink-0 rounded-2xl object-cover shadow-2xl shadow-black/50"
                />
            )}
            <div className="flex flex-col gap-[1vh]">
                <span className="animate-rise text-[clamp(1rem,2.4vh,1.875rem)] uppercase text-white/60">
                    A resposta era
                </span>
                <span className="animate-pop text-balance text-[clamp(2rem,6.5vh,5rem)] font-bold leading-tight text-amber-300">
                    {ended.answer}
                </span>
                {ended.artist && (
                    <span
                        className="animate-rise text-[clamp(1.25rem,3.4vh,2.5rem)] text-white/80"
                        style={{ animationDelay: "200ms" }}
                    >
                        {ended.artist}
                    </span>
                )}
            </div>
        </div>

        {/* O tempo até a próxima rodada aparece na barra do letreiro. */}
        {ended.lastRound && (
            <p className="text-[clamp(1rem,2.4vh,1.875rem)] text-white/60">Resultado final em instantes…</p>
        )}
    </div>
);

export default RevealPanel;
