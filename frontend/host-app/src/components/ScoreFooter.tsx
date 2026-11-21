import { useRef } from "react";

import { useFlipAnimation } from "../hooks/useFlipAnimation";
import type { RoomState, RoundView } from "../types/protocol";
import { formatPoints, positionChange } from "../utils/round.utils";
import AvatarFrame from "./AvatarFrame";
import PositionChange from "./PositionChange";

interface IScoreFooter {
    room: RoomState;
    round: RoundView | null;
}

/** A partir de quantos jogadores os cards ficam mais estreitos, para caber todo mundo numa linha. */
const COMPACT_FROM = 9;

/**
 * Ranking no rodapé da partida. Durante a rodada mostra os pontos até a rodada anterior e quem já respondeu; na
 * revelação mostra o placar atualizado, quantas posições cada um subiu ou desceu e os pontos ganhos na rodada; os cards
 * deslizam até a nova posição.
 */
const ScoreFooter = ({ room, round }: IScoreFooter) => {
    const ended = round?.phase === "REVEAL" ? round.ended : null;
    const scoreboard = ended ? ended.scoreboard : (round?.previousScoreboard ?? []);
    const scoreOf = (playerId: string) => scoreboard.find((score) => score.playerId === playerId)?.score ?? 0;
    const players = [...room.players].sort(
        (a, b) => scoreOf(b.id) - scoreOf(a.id) || a.nickname.localeCompare(b.nickname),
    );
    const compact = players.length >= COMPACT_FROM;
    const pointsOf = (playerId: string) => ended?.results.find((result) => result.playerId === playerId)?.points ?? 0;
    const footerRef = useRef<HTMLElement>(null);
    useFlipAnimation(footerRef, players.map((player) => player.id).join());

    return (
        <footer ref={footerRef} className="flex shrink-0 justify-center gap-[0.8vw] px-[max(6vw,5.5rem)] pb-[1.5vh]">
            {players.map((player, index) => {
                const answered = round?.phase === "PLAYING" && round.answeredPlayerIds.includes(player.id);
                const change =
                    ended && round && round.previousScoreboard.length > 0
                        ? positionChange(round.previousScoreboard, ended.scoreboard, player.id)
                        : 0;

                return (
                    <div
                        key={player.id}
                        data-flip-id={player.id}
                        className={`relative flex min-w-0 flex-1 basis-0 flex-col items-center gap-[0.4vh] rounded-2xl px-2 pb-[0.8vh] pt-[1.2vh] transition-colors ${
                            compact ? "max-w-[8rem]" : "max-w-[11rem]"
                        } ${answered ? "bg-[#44277B]" : "bg-white/10"} ${player.connected ? "" : "opacity-40"}`}
                    >
                        {ended && pointsOf(player.id) > 0 && (
                            <span className="pointer-events-none absolute inset-x-0 bottom-full flex justify-center">
                                <span
                                    key={ended.round}
                                    className="animate-score-gain text-[clamp(0.9rem,3vh,1.75rem)] font-extrabold text-emerald-400 drop-shadow"
                                >
                                    +{formatPoints(pointsOf(player.id))}
                                </span>
                            </span>
                        )}
                        <span className="absolute left-[0.6vw] top-[0.6vh] text-[clamp(0.625rem,1.6vh,0.875rem)] italic text-white/50">
                            {index + 1}
                        </span>
                        {answered && (
                            <span
                                className="absolute right-[0.6vw] top-[0.4vh] text-[clamp(0.75rem,2.2vh,1.125rem)] font-bold text-emerald-400"
                                title="Respondeu"
                            >
                                ✓
                            </span>
                        )}
                        {change !== 0 && (
                            <PositionChange
                                change={change}
                                className="absolute right-0 top-[0.4vh] text-[clamp(0.625rem,1.6vh,0.875rem)]"
                            />
                        )}
                        <AvatarFrame
                            avatar={player.avatar}
                            className="h-[clamp(1.75rem,7vh,5rem)] w-[clamp(1.75rem,7vh,5rem)]"
                        />
                        <span className="max-w-full truncate text-[clamp(0.7rem,2.1vh,1.25rem)] font-semibold text-white">
                            {player.nickname}
                        </span>
                        <span className="text-[clamp(0.65rem,1.8vh,1.125rem)] text-white/60">
                            {formatPoints(scoreOf(player.id))}
                        </span>
                    </div>
                );
            })}
        </footer>
    );
};

export default ScoreFooter;
