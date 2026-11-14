import ConnectionBanner from "../components/ConnectionBanner";
import TimeBar from "../components/TimeBar";
import { useCountdown } from "../hooks/useCountdown";
import { usePlayer } from "../hooks/usePlayer";
import type { PlayerSession, RoomState, RoundView } from "../types/protocol";
import { formatPoints, ordinal, positionChange } from "../utils/round.utils";

interface IGame {
    room: RoomState;
    session: PlayerSession;
}

/** Cores das 4 opções, para o jogador achar a sua escolha rápido. */
const OPTION_COLORS = ["bg-rose-500", "bg-sky-500", "bg-amber-500", "bg-emerald-500"];

export default function Game({ room, session }: IGame) {
    const { round, connection } = usePlayer();
    const remainingMs = useCountdown(round ? round.endsAt : null);

    return (
        <div className="flex min-h-screen flex-col">
            <ConnectionBanner status={connection} hostConnected={room.connected} />

            <div className="flex grow flex-col gap-6 px-6 py-8">
                {round && (
                    <header className="text-center text-sm uppercase text-white/60">
                        Rodada {round.round} de {round.totalRounds}
                    </header>
                )}
                <RoundContent room={room} session={session} round={round} remainingMs={remainingMs} />
            </div>
        </div>
    );
}

interface IRoundContent {
    room: RoomState;
    session: PlayerSession;
    round: RoundView | null;
    remainingMs: number;
}

function RoundContent({ room, session, round, remainingMs }: IRoundContent) {
    const { myAnswer, submitAnswer } = usePlayer();

    if (!round || round.phase === "COUNTDOWN") {
        return (
            <div className="flex grow flex-col items-center justify-center gap-4 text-center">
                <span className="text-2xl font-bold">Prepare-se!</span>
                {round && (
                    <span
                        key={`${round.round}-${Math.ceil(remainingMs / 1000)}`}
                        className="animate-pop text-8xl font-bold text-amber-300"
                    >
                        {Math.max(1, Math.ceil(remainingMs / 1000))}
                    </span>
                )}
                <span className="text-white/60">Preste atenção na música que vai tocar na TV.</span>
            </div>
        );
    }

    const selected = myAnswer?.round === round.round ? myAnswer.option : null;

    if (round.phase === "PLAYING") {
        const answered = selected !== null || round.answeredPlayerIds.includes(session.playerId);
        const connected = room.players.filter((player) => player.connected).length;

        return (
            <div className="flex grow flex-col gap-5">
                <TimeBar remainingMs={remainingMs} totalMs={round.phaseDurationMs} />
                <div className="flex grow flex-col gap-3">
                    {round.options.map((option, index) => (
                        <button
                            key={option}
                            type="button"
                            disabled={answered}
                            className={`grow rounded-2xl px-4 py-4 text-lg font-bold leading-tight transition ${OPTION_COLORS[index]} ${
                                answered && selected !== index ? "opacity-30" : ""
                            } ${selected === index ? "ring-4 ring-white" : ""}`}
                            onClick={() => submitAnswer(index)}
                        >
                            {option}
                        </button>
                    ))}
                </div>
                <p className="text-center text-white/70">
                    {answered
                        ? `Resposta enviada! Aguardando os outros… (${round.answeredPlayerIds.length} de ${connected})`
                        : "Qual música está tocando?"}
                </p>
            </div>
        );
    }

    const ended = round.ended;
    if (!ended) {
        return null;
    }

    const mine = ended.results.find((result) => result.playerId === session.playerId);
    const position = ended.scoreboard.findIndex((score) => score.playerId === session.playerId);
    const myScore = position >= 0 ? ended.scoreboard[position].score : 0;
    const change =
        round.previousScoreboard.length > 0
            ? positionChange(round.previousScoreboard, ended.scoreboard, session.playerId)
            : 0;

    return (
        <div className="flex grow flex-col gap-5">
            <div
                className={`flex flex-col items-center gap-1 rounded-2xl p-5 text-center ${
                    mine?.correct ? "animate-pop bg-emerald-500" : "animate-shake bg-white/10"
                }`}
            >
                <span className="text-2xl font-bold">
                    {mine?.correct ? "Você acertou!" : mine?.answered ? "Não foi dessa vez" : "Tempo esgotado"}
                </span>
                {mine?.correct && <span className="text-3xl font-bold">+{formatPoints(mine.points)}</span>}
            </div>

            <div className="animate-rise flex items-center gap-4">
                {ended.cover && (
                    <img src={ended.cover} alt="" className="aspect-square w-20 shrink-0 rounded-xl object-cover" />
                )}
                <div className="flex min-w-0 flex-col gap-0.5">
                    <span className="text-sm uppercase text-white/60">A resposta era</span>
                    <span className="text-xl font-bold leading-tight text-amber-300">{ended.answer}</span>
                    {ended.artist && <span className="truncate text-white/70">{ended.artist}</span>}
                </div>
            </div>

            {round.options.length > 0 && (
                <div className="flex flex-col gap-2">
                    {round.options.map((option, index) => (
                        <div
                            key={option}
                            className={`rounded-xl px-4 py-3 text-center font-semibold ${
                                option === ended.answer
                                    ? "bg-emerald-500"
                                    : selected === index
                                      ? "bg-red-500"
                                      : "bg-white/5 text-white/40"
                            }`}
                        >
                            {option}
                        </div>
                    ))}
                </div>
            )}

            {position >= 0 && (
                <p className="mt-auto text-center text-white/80">
                    Você está em <strong>{ordinal(position + 1)}</strong> lugar com{" "}
                    <strong>{formatPoints(myScore)}</strong> pontos
                    {change !== 0 && (
                        <span className={`ml-2 font-bold ${change > 0 ? "text-emerald-400" : "text-red-400"}`}>
                            {change > 0 ? `▲${change}` : `▼${-change}`}
                        </span>
                    )}
                </p>
            )}
        </div>
    );
}
