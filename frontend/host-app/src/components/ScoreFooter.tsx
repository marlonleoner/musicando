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
 * revelação mostra o placar atualizado e quantas posições cada um subiu ou desceu.
 */
const ScoreFooter = ({ room, round }: IScoreFooter) => {
    const ended = round?.phase === "REVEAL" ? round.ended : null;
    const scoreboard = ended ? ended.scoreboard : (round?.previousScoreboard ?? []);
    const scoreOf = (playerId: string) => scoreboard.find((score) => score.playerId === playerId)?.score ?? 0;
    const players = [...room.players].sort(
        (a, b) => scoreOf(b.id) - scoreOf(a.id) || a.nickname.localeCompare(b.nickname),
    );
    const compact = players.length >= COMPACT_FROM;

    return (
        <footer className="flex shrink-0 flex-wrap justify-center gap-3 px-24 pb-4">
            {players.map((player, index) => {
                const answered = round?.phase === "PLAYING" && round.answeredPlayerIds.includes(player.id);
                const change =
                    ended && round && round.previousScoreboard.length > 0
                        ? positionChange(round.previousScoreboard, ended.scoreboard, player.id)
                        : 0;

                return (
                    <div
                        key={player.id}
                        className={`relative flex flex-col items-center gap-1 rounded-2xl px-2 pb-2 pt-3 transition-colors ${
                            compact ? "w-28" : "w-36 2xl:w-44"
                        } ${answered ? "bg-[#44277B]" : "bg-white/10"} ${player.connected ? "" : "opacity-40"}`}
                    >
                        <span className="absolute left-3 top-2 text-sm italic text-white/50">{index + 1}</span>
                        {answered && (
                            <span
                                className="absolute right-3 top-1.5 text-lg font-bold text-emerald-400"
                                title="Respondeu"
                            >
                                ✓
                            </span>
                        )}
                        {change !== 0 && (
                            <PositionChange change={change} className="absolute right-0 top-1.5 text-sm" />
                        )}
                        <AvatarFrame
                            avatar={player.avatar}
                            className={compact ? "h-12 w-12" : "h-14 w-14 2xl:h-20 2xl:w-20"}
                        />
                        <span className="max-w-full truncate text-base font-semibold text-white 2xl:text-xl">
                            {player.nickname}
                        </span>
                        <span className="text-sm text-white/60 2xl:text-lg">{formatPoints(scoreOf(player.id))}</span>
                    </div>
                );
            })}
        </footer>
    );
};

export default ScoreFooter;
