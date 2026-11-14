import { Toaster } from "sonner";
import ConnectionBanner from "./components/ConnectionBanner";
import { GameProvider } from "./context/GameContext";
import { ThemeProvider } from "./context/ThemeContext";
import { useGame } from "./hooks/useGame";
import Home from "./pages/Home";
import Lobby from "./pages/Lobby";
import StatusScreen from "./pages/StatusScreen";

function Screen() {
    const { session, room, connection, reclaimScreen } = useGame();

    if (!session) {
        return <Home />;
    }

    if (connection === "replaced") {
        return (
            <StatusScreen
                title="Sala aberta em outra tela"
                description={`A sala ${session.code} foi aberta em outra tela ou aba e esta foi desconectada.`}
                action={{ label: "Usar esta tela", onClick: reclaimScreen }}
            />
        );
    }

    if (!room) {
        return <StatusScreen title="Conectando à sala…" />;
    }

    if (room.status === "WAITING") {
        return <Lobby room={room} />;
    }

    return (
        <StatusScreen
            title={room.status === "PAUSED" ? "Partida pausada" : "Partida em andamento"}
            description="A tela da partida chega na próxima fase do projeto."
        />
    );
}

function App() {
    return (
        <>
            <Toaster position="top-right" richColors theme="dark" />
            <ThemeProvider>
                <GameProvider>
                    <>
                        <ConnectionStatus />
                        <Screen />
                    </>
                </GameProvider>
            </ThemeProvider>
        </>
    );
}

function ConnectionStatus() {
    const { session, connection } = useGame();

    return session ? <ConnectionBanner status={connection} /> : null;
}

export default App;
