import { useEffect } from "react";
import CloseRoomButton from "../components/CloseRoomButton";
import FastestSidebar from "../components/FastestSidebar";
import Jumbotron from "../components/Jumbotron";
import RevealPanel from "../components/RevealPanel";
import RoundDots from "../components/RoundDots";
import RoundTimer from "../components/RoundTimer";
import ScoreFooter from "../components/ScoreFooter";
import { useCountdown } from "../hooks/useCountdown";
import { useGame } from "../hooks/useGame";
import { useRoundAudio } from "../hooks/useRoundAudio";
import type { RoomState, RoundView } from "../types/protocol";
import { OPTION_STYLES } from "../utils/option.utils";
import { playFailure, playSuccess, playTick } from "../utils/sound.utils";

interface IGame {
    room: RoomState;
}

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
                    className="absolute left-[2vw] top-[2vh] z-20 max-w-[17vw] rounded-xl bg-amber-400 px-[1vw] py-[1vh] text-left text-[clamp(0.75rem,1.9vh,1.125rem)] font-bold text-black"
                    onClick={unlock}
                >
                    O navegador bloqueou o som. Clique para ativar
                </button>
            )}

            <main className="flex min-h-0 grow flex-col items-center justify-center-safe gap-[2vh] overflow-hidden px-10 pb-[1.5vh] pt-[calc(4vw+9vh+1rem)]">
                <RoundContent
                    room={room}
                    round={round}
                    remainingMs={remainingMs}
                    hasPreview={Boolean(audio?.previewUrl)}
                />
            </main>

            {round?.phase === "REVEAL" && round.ended && <FastestSidebar ended={round.ended} players={room.players} />}

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
                <span className="text-[clamp(1rem,3.4vh,2.25rem)] uppercase text-white/60">Prepare-se!</span>
                <span
                    key={`${round.round}-${Math.ceil(remainingMs / 1000)}`}
                    className="animate-pop text-[clamp(4rem,28vh,16rem)] font-bold leading-none text-amber-300"
                >
                    {Math.max(1, Math.ceil(remainingMs / 1000))}
                </span>
            </>
        );
    }

    if (round.phase === "REVEAL" && round.ended) {
        return <RevealPanel ended={round.ended} players={room.players} />;
    }

    const connected = room.players.filter((player) => player.connected);
    const answered = connected.filter((player) => round.answeredPlayerIds.includes(player.id)).length;

    return (
        <div className="flex w-full max-w-[min(80rem,90vw)] flex-col items-center gap-[2vh]">
            <span className="animate-pop text-[clamp(1.25rem,5vh,3.75rem)] font-bold text-white">Qual é a música?</span>
            <RoundTimer remainingMs={remainingMs} totalMs={round.phaseDurationMs} />
            {!hasPreview && (
                <p className="text-[clamp(0.75rem,2vh,1.125rem)] text-white/60">Prévia indisponível nesta rodada</p>
            )}
            {/* Linhas de mesma altura: todas as opções ficam do tamanho da maior. */}
            <ul className="grid w-full auto-rows-fr grid-cols-2 gap-[1.6vh]">
                {round.options.map((option, index) => (
                    <li
                        key={option}
                        className={`animate-rise flex items-center gap-[1.2vw] rounded-2xl border border-l-8 border-white/10 bg-white/5 px-[1.5vw] py-[1.4vh] ${OPTION_STYLES[index]?.border ?? ""}`}
                        style={{ animationDelay: `${index * 80}ms` }}
                    >
                        <span
                            className={`flex h-[clamp(1.5rem,5vh,3.5rem)] w-[clamp(1.5rem,5vh,3.5rem)] shrink-0 items-center justify-center rounded-lg text-[clamp(0.875rem,2.6vh,1.875rem)] font-bold text-white ${OPTION_STYLES[index]?.badge ?? "bg-white/20"}`}
                        >
                            {OPTION_STYLES[index]?.letter}
                        </span>
                        <span className="text-[clamp(0.875rem,3.2vh,2.25rem)] font-bold leading-tight text-white">
                            {option}
                        </span>
                    </li>
                ))}
            </ul>
            <span className="text-[clamp(0.875rem,2.4vh,1.875rem)] text-white/70">
                {answered} de {connected.length} responderam
            </span>
        </div>
    );
}
