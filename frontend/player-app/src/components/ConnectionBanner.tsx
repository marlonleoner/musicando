import type { ConnectionStatus } from "../hooks/usePlayerSocket";

interface IConnectionBanner {
    status: ConnectionStatus;
    hostConnected: boolean;
}

const messageFor = (status: ConnectionStatus, hostConnected: boolean): string | null => {
    if (status === "connecting") {
        return "Conectando…";
    }

    if (status === "reconnecting") {
        return "Conexão perdida. Reconectando…";
    }

    return status === "connected" && !hostConnected ? "A TV desconectou. Aguardando ela voltar…" : null;
};

const ConnectionBanner = ({ status, hostConnected }: IConnectionBanner) => {
    const message = messageFor(status, hostConnected);

    return message ? (
        <div className="bg-amber-500 px-4 py-2 text-center text-sm font-semibold text-black">{message}</div>
    ) : null;
};

export default ConnectionBanner;
