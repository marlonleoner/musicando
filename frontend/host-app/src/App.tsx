import { Toaster } from "sonner";
import ConnectionBanner from "./components/ConnectionBanner";
import { GameProvider } from "./context/GameContext";
import { ThemeProvider } from "./context/ThemeContext";
import { useGame } from "./hooks/useGame";
import FinalRanking from "./pages/FinalRanking";
import Game from "./pages/Game";
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

    if (room.status === "PAUSED") {
        return <StatusScreen title="Partida pausada" />;
    }

    if (room.status === "FINISHED") {
        return <FinalRanking room={room} />;
    }

    return <Game room={room} />;
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
