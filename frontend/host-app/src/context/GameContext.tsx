import { useCallback, useMemo, useRef, useState } from "react";
import { toast } from "sonner";
import { useHostSocket } from "../hooks/useHostSocket";
import type { ErrorPayload, HostSession, RoomState, RoomStateReason } from "../types/protocol";
import type { Element } from "../types/types";
import { GameContext, type IGameContext } from "./contexts";
import { apiErrorMessage, createRoom as createRoomRequest } from "../utils/api.utils";
import { clearHostSession, loadHostSession, saveHostSession } from "../utils/storage.utils";

export const GameProvider = ({ children }: Element) => {
    const [session, setSession] = useState<HostSession | null>(loadHostSession);
    const [room, setRoom] = useState<RoomState | null>(null);
    const [creatingRoom, setCreatingRoom] = useState(false);
    const roomRef = useRef<RoomState | null>(null);

    const resetToHome = useCallback(() => {
        clearHostSession();
        roomRef.current = null;
        setRoom(null);
        setSession(null);
    }, []);

    const handleRoomState = useCallback(
        (state: RoomState, reason?: RoomStateReason) => {
            const previous = roomRef.current;
            if (state.status === "CLOSED") {
                toast.info("Sala encerrada");
                resetToHome();

                return;
            }

            const nicknameOf = (players: RoomState["players"] | undefined) =>
                players?.find((player) => player.id === reason?.participantId)?.nickname;

            if (reason?.type === "PLAYER_JOINED") {
                toast.success(`${nicknameOf(state.players) ?? "Um jogador"} entrou na sala`);
            } else if (reason?.type === "PLAYER_LEFT") {
                toast.info(`${nicknameOf(previous?.players) ?? "Um jogador"} saiu da sala`);
            } else if (reason?.type === "PLAYER_KICKED") {
                toast.info(`${nicknameOf(previous?.players) ?? "O jogador"} foi removido da sala`);
            }

            roomRef.current = state;
            setRoom(state);
        },
        [resetToHome],
    );

    const handleCommandError = useCallback((error: ErrorPayload) => toast.error(error.message), []);

    const handleInvalidSession = useCallback(() => {
        toast.error("Esta sala não existe mais. Crie uma nova sala.");
        resetToHome();
    }, [resetToHome]);

    const { status, sendCommand, reclaim } = useHostSocket({
        session,
        onRoomState: handleRoomState,
        onCommandError: handleCommandError,
        onInvalidSession: handleInvalidSession,
    });

    const createRoom = useCallback(async () => {
        setCreatingRoom(true);
        try {
            const created = await createRoomRequest();
            saveHostSession(created);
            setSession(created);
        } catch (error) {
            toast.error(apiErrorMessage(error));
        } finally {
            setCreatingRoom(false);
        }
    }, []);

    const kickPlayer = useCallback(
        (playerId: string) => {
            if (!sendCommand("KICK_PLAYER", { playerId })) {
                toast.error("Sem conexão com o servidor. Tente novamente em instantes.");
            }
        },
        [sendCommand],
    );

    const closeRoom = useCallback(() => {
        if (!sendCommand("CLOSE_ROOM")) {
            toast.error("Sem conexão com o servidor. Tente novamente em instantes.");
        }
    }, [sendCommand]);

    const value = useMemo<IGameContext>(
        () => ({
            session,
            room,
            connection: status,
            creatingRoom,
            createRoom,
            kickPlayer,
            closeRoom,
            reclaimScreen: reclaim,
        }),
        [session, room, status, creatingRoom, createRoom, kickPlayer, closeRoom, reclaim],
    );

    return <GameContext.Provider value={value}>{children}</GameContext.Provider>;
};
