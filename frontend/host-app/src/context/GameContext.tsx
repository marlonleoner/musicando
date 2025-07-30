import { createContext, useCallback, useState } from "react";
import type { Element, IGameContext, IPlayer, IRoom } from "../types/types";
import { api } from "@utils/api.utils";
import type { CreateRoomResponse } from "src/types/response";
import { toast } from "sonner";
import type { AxiosError, AxiosResponse } from "axios";

export const GameContext = createContext<IGameContext>(null as unknown as IGameContext);

export const GameProvider = ({ children }: Element) => {
    // const [state, dispatch] = useReducer(useGameReducer as Reducer, initialGameContext);

    const [room, setRoom] = useState<IRoom | undefined>(undefined);
    const [players, setPlayers] = useState<IPlayer[]>([]);

    const createRoom = useCallback(async () => {
        try {
            const { data } = await api.post("/room");
            const { id, code, secret } = data as CreateRoomResponse;

            setRoom({
                id,
                code,
                secret,
            });

            toast.success("Sala criada com sucesso");
        } catch (error) {
            const { response } = error as AxiosError;
            const { data } = response as AxiosResponse;

            toast.error(data.message);
        }
    }, []);

    const value: IGameContext = {
        room,
        players,
        createRoom,
        joinPlayer: (player: IPlayer) => setPlayers((prev) => [...prev, player]),
    };

    return <GameContext.Provider value={value}>{children}</GameContext.Provider>;
};
