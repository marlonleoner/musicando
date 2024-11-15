import { useContext } from "react";
import { GameContext } from "../../context/GameContext";
import { RoundState } from "../../types/enums";
import Alternative from "../Alternative";
import RoundLive from "../RoundLive";
import RoundPreLive from "../RoundPreLive";
import RoundSummary from "../RoundSummary";

const GameLayout = () => {
    const { round, response } = useContext(GameContext);

    return (
        <>
            <pre>{JSON.stringify(response)}</pre>
            {(() => {
                switch (round?.state) {
                    case RoundState.PRE_LIVE:
                        return <RoundPreLive round={round.id} />;
                    case RoundState.LIVE:
                        return <RoundLive alternatives={round.alternatives} />;
                    case RoundState.FINISHED:
                        return <Alternative alternative={round.answer} correctAnswer={response?.correctAnswer} />;
                    case RoundState.SUMMARY:
                        return <RoundSummary answer={round.answer} response={response} />;
                    default:
                        return <p>State não mapeado</p>;
                }
            })()}
        </>
    );
};

export default GameLayout;
