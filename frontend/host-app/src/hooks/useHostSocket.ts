import { Client, type IMessage } from "@stomp/stompjs";
import { useCallback, useEffect, useRef, useState } from "react";
import type {
    ErrorPayload,
    HostSession,
    RoomState,
    RoomStateReason,
    SocketCommandType,
    SocketEvent,
} from "../types/protocol";
import { socketUrl } from "../utils/env.utils";

/** Fechamento enviado pelo backend quando a mesma sala é aberta em outra tela. */
const SESSION_REPLACED_CLOSE_CODE = 4001;

const RECONNECT_DELAY_MS = 3000;

export type ConnectionStatus = "connecting" | "connected" | "reconnecting" | "replaced";

interface Options {
    session: HostSession | null;
    onRoomState: (state: RoomState, reason?: RoomStateReason) => void;
    onCommandError: (error: ErrorPayload) => void;
    onInvalidSession: () => void;
}

/**
 * Conexão STOMP do host. Autentica com as credenciais da sala, assina os canais da sala e mantém apenas a foto mais
 * recente da sala (pelo timestamp do evento). Reconecta sozinho em caso de queda.
 */
export const useHostSocket = ({ session, onRoomState, onCommandError, onInvalidSession }: Options) => {
    const [status, setStatus] = useState<ConnectionStatus>("connecting");
    const clientRef = useRef<Client | null>(null);
    const lastStateAtRef = useRef<number>(0);
    const callbacksRef = useRef({ onRoomState, onCommandError, onInvalidSession });
    const [attempt, setAttempt] = useState(0);

    useEffect(() => {
        callbacksRef.current = { onRoomState, onCommandError, onInvalidSession };
    }, [onRoomState, onCommandError, onInvalidSession]);

    useEffect(() => {
        if (!session) {
            return;
        }

        lastStateAtRef.current = 0;
        setStatus("connecting");

        const handleEvent = (message: IMessage) => {
            const event = JSON.parse(message.body) as SocketEvent;
            if (event.type === "ROOM_STATE") {
                const at = Date.parse(event.timestamp);
                if (at < lastStateAtRef.current) {
                    return;
                }

                lastStateAtRef.current = at;
                callbacksRef.current.onRoomState(event.payload, event.reason);
            } else if (event.type === "ERROR") {
                callbacksRef.current.onCommandError(event.payload);
            }
        };

        const client = new Client({
            brokerURL: socketUrl(),
            connectHeaders: { role: "host", code: session.code, id: session.id, secret: session.secret },
            reconnectDelay: RECONNECT_DELAY_MS,
            heartbeatIncoming: 0,
            heartbeatOutgoing: 0,
            onConnect: () => {
                setStatus("connected");
                client.subscribe("/user/queue/events", handleEvent);
                client.subscribe("/user/queue/errors", handleEvent);
                client.subscribe(`/topic/room/${session.code}`, handleEvent);
                client.subscribe(`/topic/room/${session.code}/host`, handleEvent);
                client.subscribe("/musicando/room", handleEvent);
            },
            onStompError: (frame) => {
                if (frame.headers.message === "INVALID_CREDENTIALS") {
                    void client.deactivate();
                    callbacksRef.current.onInvalidSession();
                }
            },
            onWebSocketClose: (event: CloseEvent) => {
                if (event.code === SESSION_REPLACED_CLOSE_CODE) {
                    void client.deactivate();
                    setStatus("replaced");

                    return;
                }

                setStatus((current) => (current === "replaced" ? current : "reconnecting"));
            },
        });

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

    /** Volta a usar esta tela depois que a sala foi aberta em outra (a outra tela é que será desconectada). */
    const reclaim = useCallback(() => setAttempt((value) => value + 1), []);

    return { status, sendCommand, reclaim };
};
