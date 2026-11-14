import type { ConnectionStatus } from "../hooks/useHostSocket";

interface IConnectionBanner {
    status: ConnectionStatus;
}

const ConnectionBanner = ({ status }: IConnectionBanner) => {
    if (status === "connected" || status === "replaced") {
        return null;
    }

    return (
        <div className="fixed left-1/2 top-4 -translate-x-1/2 rounded-full bg-amber-500 px-4 py-1 text-sm font-semibold text-black">
            {status === "connecting" ? "Conectando ao servidor…" : "Conexão perdida. Reconectando…"}
        </div>
    );
};

export default ConnectionBanner;
