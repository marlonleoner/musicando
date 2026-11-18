import { useEffect } from "react";
import CloseRoomButton from "../components/CloseRoomButton";
import Jumbotron from "../components/Jumbotron";
import RevealPanel from "../components/RevealPanel";
import RoundDots from "../components/RoundDots";
import RoundTimer from "../components/RoundTimer";
import ScoreFooter from "../components/ScoreFooter";
import { useCountdown } from "../hooks/useCountdown";
import { useGame } from "../hooks/useGame";
import { useRoundAudio } from "../hooks/useRoundAudio";
import type { RoomState, RoundView } from "../types/protocol";
import { playFailure, playSuccess, playTick } from "../utils/sound.utils";

interface IGame {
    room: RoomState;
}

/** Cores e letras das opções, iguais às dos botões do celular, para o jogador achar a sua escolha. */
const OPTIONS = [
    { letter: "A", badge: "bg-rose-500", border: "border-l-rose-500" },
    { letter: "B", badge: "bg-sky-500", border: "border-l-sky-500" },
    { letter: "C", badge: "bg-amber-500", border: "border-l-amber-500" },
    { letter: "D", badge: "bg-emerald-500", border: "border-l-emerald-500" },
];

export default function Game({ room }: IGame) {
    const { round, audio, closeRoom } = useGame();
    const { blocked, unlock } = useRoundAudio(audio, round);
    const remainingMs = useCountdown(round ? round.endsAt : null, round?.phaseDurationMs);
    const countdownSecond = round?.phase === "COUNTDOWN" ? Math.ceil(remainingMs / 1000) : null;
    const revealedRound = round?.phase === "REVEAL" && round.ended ? round.round : null;
    const someoneGotIt = Boolean(round?.ended && round.ended.fastest.length > 0);

    useEffect(() => {
        if (countdownSecond !== null && countdownSecond > 0) {
            playTick(countdownSecond === 1);
        }
    }, [countdownSecond, round?.round]);

    useEffect(() => {
        if (revealedRound === null) {
            return;
        }

        if (someoneGotIt) {
            playSuccess();
        } else {
            playFailure();
        }
    }, [revealedRound, someoneGotIt]);

    return (
        // A tela tem sempre a altura da janela: o conteúdo se ajusta entre o letreiro e o placar do rodapé.
        <div className="relative flex h-dvh w-full flex-col overflow-hidden">
            <Jumbotron remainingMs={remainingMs} totalMs={round?.phaseDurationMs ?? 0}>
                {round && <RoundDots round={round.round} totalRounds={round.totalRounds} />}
            </Jumbotron>
            <div className="absolute right-8 top-6 z-20">
                <CloseRoomButton onClose={closeRoom} />
            </div>

            {blocked && (
                <button
                    type="button"
                    className="absolute left-8 top-6 z-20 max-w-xs rounded-xl bg-amber-400 px-5 py-3 text-left text-lg font-bold text-black"
                    onClick={unlock}
                >
                    O navegador bloqueou o som. Clique para ativar
                </button>
            )}

            <main className="flex min-h-0 grow flex-col items-center justify-center gap-5 px-10 pb-4 pt-40 2xl:pt-44">
                <RoundContent
                    room={room}
                    round={round}
                    remainingMs={remainingMs}
                    hasPreview={Boolean(audio?.previewUrl)}
                />
            </main>

            <ScoreFooter room={room} round={round} />
        </div>
    );
}

interface IRoundContent {
    room: RoomState;
    round: RoundView | null;
    remainingMs: number;
    hasPreview: boolean;
}

function RoundContent({ room, round, remainingMs, hasPreview }: IRoundContent) {
    if (!round) {
        return <h1 className="animate-pop text-5xl font-bold text-white 2xl:text-7xl">A partida vai começar!</h1>;
    }

    if (round.phase === "COUNTDOWN") {
        return (
            <>
                <span className="text-2xl uppercase text-white/60 2xl:text-4xl">Prepare-se!</span>
                <span
                    key={`${round.round}-${Math.ceil(remainingMs / 1000)}`}
                    className="animate-pop text-[10rem] font-bold leading-none text-amber-300 2xl:text-[16rem]"
                >
                    {Math.max(1, Math.ceil(remainingMs / 1000))}
                </span>
            </>
        );
    }

    if (round.phase === "REVEAL" && round.ended) {
        return <RevealPanel ended={round.ended} remainingMs={remainingMs} players={room.players} />;
    }

    const connected = room.players.filter((player) => player.connected);
    const answered = connected.filter((player) => round.answeredPlayerIds.includes(player.id)).length;

    return (
        <div className="flex w-full max-w-5xl flex-col items-center gap-5 2xl:max-w-7xl">
            <span className="animate-pop text-4xl font-bold text-white 2xl:text-6xl">Qual é a música?</span>
            <RoundTimer remainingMs={remainingMs} totalMs={round.phaseDurationMs} />
            {!hasPreview && <p className="text-lg text-white/60">Prévia indisponível nesta rodada</p>}
            {/* Linhas de mesma altura: todas as opções ficam do tamanho da maior. */}
            <ul className="grid w-full auto-rows-fr grid-cols-2 gap-4 2xl:gap-6">
                {round.options.map((option, index) => (
                    <li
                        key={option}
                        className={`animate-rise flex items-center gap-4 rounded-2xl border border-l-8 border-white/10 bg-white/5 px-6 py-4 2xl:py-5 ${OPTIONS[index]?.border ?? ""}`}
                        style={{ animationDelay: `${index * 80}ms` }}
                    >
                        <span
                            className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-lg text-xl font-bold text-white 2xl:h-14 2xl:w-14 2xl:text-3xl ${OPTIONS[index]?.badge ?? "bg-white/20"}`}
                        >
                            {OPTIONS[index]?.letter}
                        </span>
                        <span className="text-2xl font-bold text-white 2xl:text-4xl">{option}</span>
                    </li>
                ))}
            </ul>
            <span className="text-xl text-white/70 2xl:text-3xl">
                {answered} de {connected.length} responderam
            </span>
        </div>
    );
}
