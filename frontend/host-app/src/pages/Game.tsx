import { useEffect, useState } from "react";
import Avatar from "../components/Avatar";
import Equalizer from "../components/Equalizer";
import Logo from "../components/Logo";
import RevealPanel from "../components/RevealPanel";
import TimeBar from "../components/TimeBar";
import { useCountdown } from "../hooks/useCountdown";
import { useGame } from "../hooks/useGame";
import { useRoundAudio } from "../hooks/useRoundAudio";
import type { RoomState, RoundView } from "../types/protocol";
import { playFailure, playSuccess, playTick } from "../utils/sound.utils";

interface IGame {
    room: RoomState;
}

export default function Game({ room }: IGame) {
    const { round, audio, closeRoom } = useGame();
    const { blocked, unlock } = useRoundAudio(audio, round);
    const remainingMs = useCountdown(round ? round.endsAt : null, round?.phaseDurationMs);
    const [confirmClose, setConfirmClose] = useState(false);
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
        <div className="flex min-h-screen w-full flex-col gap-8 p-10">
            <header className="flex items-center justify-between">
                <Logo size="small" />
                {round && (
                    <span className="text-2xl font-semibold text-white/80 2xl:text-4xl">
                        Rodada {round.round} de {round.totalRounds}
                    </span>
                )}
                <button
                    type="button"
                    className={`rounded-lg px-4 py-2 text-sm ${confirmClose ? "bg-red-500 font-bold" : "text-white/50 hover:text-white"}`}
                    onClick={() => (confirmClose ? closeRoom() : setConfirmClose(true))}
                    onBlur={() => setConfirmClose(false)}
                >
                    {confirmClose ? "Confirmar encerramento" : "Encerrar sala"}
                </button>
            </header>

            {blocked && (
                <button
                    type="button"
                    className="self-center rounded-xl bg-amber-400 px-6 py-3 text-xl font-bold text-black"
                    onClick={unlock}
                >
                    O navegador bloqueou o som. Clique para ativar
                </button>
            )}

            <main className="flex grow flex-col items-center justify-center gap-10">
                <RoundContent
                    room={room}
                    round={round}
                    remainingMs={remainingMs}
                    hasPreview={Boolean(audio?.previewUrl)}
                />
            </main>
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
                    className="animate-pop text-[12rem] font-bold leading-none text-amber-300 2xl:text-[18rem]"
                >
                    {Math.max(1, Math.ceil(remainingMs / 1000))}
                </span>
            </>
        );
    }

    if (round.phase === "REVEAL" && round.ended) {
        return (
            <RevealPanel
                ended={round.ended}
                previousScoreboard={round.previousScoreboard}
                remainingMs={remainingMs}
                players={room.players}
            />
        );
    }

    const connected = room.players.filter((player) => player.connected);
    const answered = connected.filter((player) => round.answeredPlayerIds.includes(player.id)).length;

    return (
        <div className="flex w-full max-w-4xl flex-col items-center gap-10 2xl:max-w-6xl">
            {hasPreview ? <Equalizer /> : <p className="text-2xl text-white/60">Prévia indisponível nesta rodada</p>}
            <span className="animate-pop text-3xl font-bold text-white 2xl:text-6xl">Qual é a música?</span>
            <TimeBar remainingMs={remainingMs} totalMs={round.phaseDurationMs} className="h-4" />
            <span className="text-2xl text-white/70 2xl:text-4xl">
                {answered} de {connected.length} responderam · {Math.ceil(remainingMs / 1000)} s
            </span>
            <ul className="flex flex-wrap justify-center gap-3">
                {connected.map((player) => (
                    <li
                        key={player.id}
                        className={`rounded-xl px-4 py-2 text-xl font-semibold transition 2xl:px-6 2xl:py-3 2xl:text-3xl ${
                            round.answeredPlayerIds.includes(player.id)
                                ? "bg-emerald-500 text-white"
                                : "bg-white/10 text-white/50"
                        }`}
                    >
                        <span className="flex items-center gap-2">
                            <Avatar avatar={player.avatar} kind="face" className="h-8 w-8 2xl:h-12 2xl:w-12" />
                            {player.nickname}
                        </span>
                    </li>
                ))}
            </ul>
        </div>
    );
}
