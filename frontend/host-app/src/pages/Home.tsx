import { useEffect, useContext } from "react";
import { GameContext } from "@context/GameContext";

export default function Home() {
    const { reconnect, createRoom } = useContext(GameContext);

    useEffect(() => {
        const gameCode = localStorage.getItem("msc:code");
        const gameSecret = localStorage.getItem("msc:secret");
        if (gameCode && gameSecret) {
            console.log("reconnecting host", gameCode, gameSecret);
            reconnect(gameCode, gameSecret);
        }
    }, []);

    return (
        <div className="grow flex flex-col justify-center items-center gap-[35vh] font-['Monoton']">
            <div className="text-[8vw] tracking-[-0.4vw] [text-shadow:2px_1px_5px_#44277B,_-2px_2px_5px_#44277B,_-3px_3px_5px_#44277B,_-4px_4px_5px_#44277B,_-5px_5px_5px_#44277B,_-6px_6px_5px_#44277B,_-7px_7px_5px_#44277B,_-8px_8px_5px_#44277B,_-9px_9px_5px_#44277B,_-10px_10px_5px_#44277B,_-11px_11px_5px_#5F3A90,_-12px_12px_5px_#5F3A90,_-13px_13px_5px_#5F3A90,_-14px_14px_5px_#5F3A90,_-15px_15px_5px_#5F3A90,_-16px_16px_5px_#5F3A90,_-17px_17px_5px_#5F3A90,_-18px_18px_5px_#5F3A90,_-19px_19px_5px_#5F3A90,_-20px_20px_5px_#5F3A90,_-21px_21px_5px_#926CAE,_-22px_22px_5px_#926CAE,_-23px_23px_5px_#926CAE,_-24px_24px_5px_#926CAE,_-25px_25px_5px_#926CAE,_-26px_26px_5px_#926CAE,_-27px_27px_5px_#926CAE,_-28px_28px_5px_#926CAE,_-29px_29px_5px_#926CAE,_-30px_30px_5px_#926CAE]">
                MUSICANDO
            </div>
            <button type="button" className="text-[2.4vw] uppercase text-white tracking-[-0.1rem]" onClick={createRoom}>
                Criar Sala
            </button>
        </div>
    );
}
