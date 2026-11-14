import { Client, type IMessage } from "@stomp/stompjs";
import { useCallback, useEffect, useRef, useState } from "react";
import type {
    ErrorPayload,
    GameEvent,
    PlayerSession,
    RoomState,
    RoomStateReason,
    SocketCommandType,
    SocketEvent,
} from "../types/protocol";
import { socketUrl } from "../utils/env.utils";

/** Fechamento enviado pelo backend quando o mesmo jogador abre o jogo em outra aba ou aparelho. */
const SESSION_REPLACED_CLOSE_CODE = 4001;

const RECONNECT_DELAY_MS = 3000;

const GAME_EVENTS: ReadonlySet<string> = new Set([
    "ROUND_COUNTDOWN",
    "ROUND_STARTED",
    "ROUND_AUDIO",
    "ANSWER_RECEIVED",
    "ROUND_ENDED",
]);

const isGameEvent = (event: SocketEvent): event is GameEvent => GAME_EVENTS.has(event.type);

export type ConnectionStatus = "connecting" | "connected" | "reconnecting" | "replaced";

interface Options {
    session: PlayerSession | null;
    onRoomState: (state: RoomState, reason?: RoomStateReason) => void;
    onGameEvent: (event: GameEvent) => void;
    onKicked: () => void;
    onCommandError: (error: ErrorPayload) => void;
    onInvalidSession: () => void;
}

/**
 * Conexão STOMP do jogador. Autentica com as credenciais recebidas ao entrar na sala, assina os canais da sala e
 * mantém apenas a foto mais recente da sala (pelo timestamp do evento). Reconecta sozinho em caso de queda.
 */
export const usePlayerSocket = ({
    session,
    onRoomState,
    onGameEvent,
    onKicked,
    onCommandError,
    onInvalidSession,
}: Options) => {
    const [status, setStatus] = useState<ConnectionStatus>("connecting");
    const [attempt, setAttempt] = useState(0);
    const clientRef = useRef<Client | null>(null);
    const lastStateAtRef = useRef<number>(0);
    const callbacksRef = useRef({ onRoomState, onGameEvent, onKicked, onCommandError, onInvalidSession });

    useEffect(() => {
        callbacksRef.current = { onRoomState, onGameEvent, onKicked, onCommandError, onInvalidSession };
    }, [onRoomState, onGameEvent, onKicked, onCommandError, onInvalidSession]);

    useEffect(() => {
        if (!session) {
            return;
        }

        lastStateAtRef.current = 0;
        setStatus("connecting");

        const client = new Client({
            brokerURL: socketUrl(),
            connectHeaders: { role: "player", code: session.roomCode, id: session.playerId, secret: session.secret },
            reconnectDelay: RECONNECT_DELAY_MS,
            heartbeatIncoming: 0,
            heartbeatOutgoing: 0,
        });

        const handleEvent = (message: IMessage) => {
            const event = JSON.parse(message.body) as SocketEvent;
            if (event.type === "ROOM_STATE") {
                const at = Date.parse(event.timestamp);
                if (at < lastStateAtRef.current) {
                    return;
                }

                lastStateAtRef.current = at;
                callbacksRef.current.onRoomState(event.payload, event.reason);
            } else if (isGameEvent(event)) {
                callbacksRef.current.onGameEvent(event);
            } else if (event.type === "KICKED") {
                void client.deactivate();
                callbacksRef.current.onKicked();
            } else if (event.type === "ERROR") {
                callbacksRef.current.onCommandError(event.payload);
            }
        };

        client.onConnect = () => {
            setStatus("connected");
            client.subscribe("/user/queue/events", handleEvent);
            client.subscribe("/user/queue/errors", handleEvent);
            client.subscribe(`/topic/room/${session.roomCode}`, handleEvent);
            client.subscribe("/musicando/room", handleEvent);
        };
        client.onStompError = (frame) => {
            if (frame.headers.message === "INVALID_CREDENTIALS") {
                void client.deactivate();
                callbacksRef.current.onInvalidSession();
            }
        };
        client.onWebSocketClose = (event: CloseEvent) => {
            if (event.code === SESSION_REPLACED_CLOSE_CODE) {
                void client.deactivate();
                setStatus("replaced");

                return;
            }

            setStatus((current) => (current === "replaced" ? current : "reconnecting"));
        };

        clientRef.current = client;
        client.activate();

        return () => {
            clientRef.current = null;
            void client.deactivate();
        };
    }, [session, attempt]);

    const sendCommand = useCallback((type: SocketCommandType, payload?: Record<string, unknown>) => {
        const client = clientRef.current;
        if (!client?.connected) {
            return false;
        }

        client.publish({ destination: "/musicando/command", body: JSON.stringify({ type, payload: payload ?? null }) });

        return true;
    }, []);

    /** Volta a usar esta aba depois que o jogo foi aberto em outra (a outra é que será desconectada). */
    const reclaim = useCallback(() => setAttempt((value) => value + 1), []);

    return { status, sendCommand, reclaim };
};
