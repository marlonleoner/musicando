import { type ReactNode, useCallback, useMemo, useState } from "react";
import { toast } from "sonner";
import { usePlayerSocket } from "../hooks/usePlayerSocket";
import type { ErrorPayload, PlayerSession, RoomState, SettingsChanges } from "../types/protocol";
import { apiErrorMessage, commandErrorMessage, joinRoom } from "../utils/api.utils";
import { codeFromUrl } from "../utils/env.utils";
import { clearPlayerSession, loadPlayerSession, savePlayerSession } from "../utils/storage.utils";
import { type ExitReason, type IPlayerContext, PlayerContext } from "./contexts";

/**
 * Sessão salva no aparelho. Se o QR code lido for de outra sala, a sessão antiga é descartada e o jogador entra na
 * sala nova.
 */
const initialSession = (): PlayerSession | null => {
    const saved = loadPlayerSession();
    const urlCode = codeFromUrl();
    if (saved && urlCode && urlCode !== saved.roomCode) {
        clearPlayerSession();

        return null;
    }

    return saved;
};

export const PlayerProvider = ({ children }: { children: ReactNode }) => {
    const [session, setSession] = useState<PlayerSession | null>(initialSession);
    const [room, setRoom] = useState<RoomState | null>(null);
    const [exitReason, setExitReason] = useState<ExitReason>(null);
    const [joining, setJoining] = useState(false);
    const [starting, setStarting] = useState(false);

    const exitRoom = useCallback((reason: ExitReason) => {
        clearPlayerSession();
        setRoom(null);
        setSession(null);
        setExitReason(reason);
    }, []);

    const handleRoomState = useCallback(
        (state: RoomState) => {
            setStarting(false);
            if (state.status === "CLOSED") {
                exitRoom("ROOM_CLOSED");

                return;
            }

            setRoom(state);
        },
        [exitRoom],
    );

    const handleKicked = useCallback(() => exitRoom("KICKED"), [exitRoom]);

    const handleCommandError = useCallback((error: ErrorPayload) => {
        setStarting(false);
        toast.error(commandErrorMessage(error));
    }, []);

    const handleInvalidSession = useCallback(() => exitRoom("SESSION_EXPIRED"), [exitRoom]);

    const { status, sendCommand, reclaim } = usePlayerSocket({
        session,
        onRoomState: handleRoomState,
        onKicked: handleKicked,
        onCommandError: handleCommandError,
        onInvalidSession: handleInvalidSession,
    });

    const join = useCallback(async (code: string, nickname: string) => {
        setJoining(true);
        try {
            const joined = await joinRoom(code, nickname);
            savePlayerSession(joined);
            setExitReason(null);
            setSession(joined);
        } catch (error) {
            toast.error(apiErrorMessage(error));
        } finally {
            setJoining(false);
        }
    }, []);

    const leave = useCallback(() => {
        sendCommand("LEAVE_ROOM");
        exitRoom(null);
    }, [sendCommand, exitRoom]);

    const updateSettings = useCallback(
        (changes: SettingsChanges) => {
            if (!sendCommand("UPDATE_SETTINGS", { ...changes })) {
                toast.error("Sem conexão com o servidor. Aguarde a reconexão.");
            }
        },
        [sendCommand],
    );

    const startGame = useCallback(() => {
        if (sendCommand("START_GAME")) {
            setStarting(true);
        } else {
            toast.error("Sem conexão com o servidor. Aguarde a reconexão.");
        }
    }, [sendCommand]);

    const dismissExitReason = useCallback(() => setExitReason(null), []);

    const value = useMemo<IPlayerContext>(
        () => ({
            session,
            room,
            connection: status,
            exitReason,
            joining,
            join,
            leave,
            updateSettings,
            startGame,
            starting,
            reclaimTab: reclaim,
            dismissExitReason,
        }),
        [
            session,
            room,
            status,
            exitReason,
            joining,
            join,
            leave,
            updateSettings,
            startGame,
            starting,
            reclaim,
            dismissExitReason,
        ],
    );

    return <PlayerContext.Provider value={value}>{children}</PlayerContext.Provider>;
};
