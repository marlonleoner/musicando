import { AnimatePresence, motion } from "framer-motion";
import { useEffect, useMemo, useState } from "react";
import { MatchState, RoundState } from "../../types/enums";
import { IMatch, IPlaylist, IRound } from "../../types/types";

interface INavbarCenter {
    match: IMatch;
    round?: IRound;
    playlist?: IPlaylist;
}

const NavbarCenter = ({ match, round, playlist }: INavbarCenter) => {
    const [countdown, setCountdown] = useState(null);

    useEffect(() => {
        setTimeout(() => {
            if (countdown > 0) setCountdown(countdown - 1);
        }, 1000);
    }, [countdown]);

    useEffect(() => {
        if (round?.state === RoundState.LIVE) {
            setCountdown(match.roundDuration);
        }
    }, [round?.state]);

    const title = useMemo(() => {
        switch (match.state) {
            case MatchState.PREPARING:
                return <span>Escolha uma playlist</span>;
            case MatchState.LIVE:
                return (
                    <>
                        <span>{playlist?.name}</span>
                        <span>
                            Rodada: {round?.id}/{match.numberOfSongs}
                        </span>
                    </>
                );
            case MatchState.FINISHED:
                return <span>Os Vencedores</span>;
            default:
                return <span>{match.state} - Estado não mapeado</span>;
        }
    }, [match.state]);

    return (
        <div className="absolute top-0 left-1/2 -translate-x-1/2 bg-white w-4/12 h-20 text-primary [clip-path:polygon(0_0,100%_0,98%_100%,2%_100%)]">
            <div className="h-4/5 text-lg font-bold tracking-tighter flex items-center justify-around">{title}</div>
            <div className="absolute bottom-0 w-full px-2">
                <AnimatePresence>
                    {round?.state === RoundState.LIVE && (
                        <motion.span
                            key="countdown"
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            exit={{ opacity: 0, transition: { duration: 0.5 } }}
                            className="absolute bottom-2 left-3 text-lg font-bold"
                        >
                            {countdown}
                        </motion.span>
                    )}
                    <motion.div
                        key="countdown-bar"
                        initial={{ width: "100%" }}
                        animate={{
                            width: round?.state === RoundState.LIVE ? 0 : "100%",
                            transition: {
                                delay: 1,
                                duration: round?.state === RoundState.LIVE ? match.roundDuration : 0,
                            },
                        }}
                        className="h-2 bg-primary"
                    ></motion.div>
                </AnimatePresence>
            </div>
        </div>
    );
};

export default NavbarCenter;
