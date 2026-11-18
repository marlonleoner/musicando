import { useCallback, useMemo, useRef, useState } from "react";
import { toast } from "sonner";
import { useHostSocket } from "../hooks/useHostSocket";
import type {
    ErrorPayload,
    GameEvent,
    HostSession,
    RoomAudio,
    RoomState,
    RoomStateReason,
    RoundView,
} from "../types/protocol";
import type { Element } from "../types/types";
import { GameContext, type IGameContext } from "./contexts";
import { apiErrorMessage, commandErrorMessage, createRoom as createRoomRequest } from "../utils/api.utils";
import { applyGameEvent } from "../utils/round.utils";
import { clearHostSession, loadHostSession, saveHostSession } from "../utils/storage.utils";

export const GameProvider = ({ children }: Element) => {
    const [session, setSession] = useState<HostSession | null>(loadHostSession);
    const [room, setRoom] = useState<RoomState | null>(null);
    const [creatingRoom, setCreatingRoom] = useState(false);
    const [round, setRound] = useState<RoundView | null>(null);
    const [audio, setAudio] = useState<RoomAudio | null>(null);
    const [lobbyAudio, setLobbyAudio] = useState<string[] | null>(null);
    const roomRef = useRef<RoomState | null>(null);

    const resetToHome = useCallback(() => {
        clearHostSession();
        roomRef.current = null;
        setRoom(null);
        setRound(null);
        setAudio(null);
        setLobbyAudio(null);
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

            if (state.status === "WAITING") {
                setRound(null);
                setAudio(null);
            } else {
                // A música do lobby pode chegar antes da sala voltar ao lobby ("Jogar novamente"), por isso só é
                // descartada quando a sala sai do lobby.
                setLobbyAudio(null);
            }

            roomRef.current = state;
            setRoom(state);
        },
        [resetToHome],
    );

    const handleGameEvent = useCallback((event: GameEvent) => {
        if (event.type === "ROUND_AUDIO") {
            setAudio({ round: event.payload.round, previewUrl: event.payload.previewUrl });

            return;
        }

        if (event.type === "LOBBY_AUDIO") {
            setLobbyAudio(event.payload.previewUrls);

            return;
        }

        setRound((current) => applyGameEvent(current, event));
    }, []);

    const handleCommandError = useCallback((error: ErrorPayload) => toast.error(commandErrorMessage(error)), []);

    const handleInvalidSession = useCallback(() => {
        toast.error("Esta sala não existe mais. Crie uma nova sala.");
        resetToHome();
    }, [resetToHome]);

    const { status, sendCommand, reclaim } = useHostSocket({
        session,
        onRoomState: handleRoomState,
        onGameEvent: handleGameEvent,
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
            round,
            audio,
            lobbyAudio,
            connection: status,
            creatingRoom,
            createRoom,
            kickPlayer,
            closeRoom,
            reclaimScreen: reclaim,
        }),
        [session, room, round, audio, lobbyAudio, status, creatingRoom, createRoom, kickPlayer, closeRoom, reclaim],
    );

    return <GameContext.Provider value={value}>{children}</GameContext.Provider>;
};
