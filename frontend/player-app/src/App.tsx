import { Toaster } from "sonner";
import { PlayerProvider } from "./context/PlayerContext";
import { usePlayer } from "./hooks/usePlayer";
import Join from "./pages/Join";
import Lobby from "./pages/Lobby";
import StatusScreen from "./pages/StatusScreen";

function Screen() {
    const { session, room, connection, reclaimTab } = usePlayer();

    if (!session) {
        return <Join />;
    }

    if (connection === "replaced") {
        return (
            <StatusScreen
                title="Jogo aberto em outra aba"
                description="Você abriu o Musicando em outra aba ou aparelho, e esta foi desconectada."
                action={{ label: "Jogar por aqui", onClick: reclaimTab }}
            />
        );
    }

    if (!room) {
        return <StatusScreen title="Entrando na sala…" />;
    }

    if (room.status === "WAITING") {
        return <Lobby room={room} session={session} />;
    }

    if (room.status === "PAUSED") {
        return <StatusScreen title="Partida pausada" description="Aguardando a TV voltar a se conectar." />;
    }

    return (
        <StatusScreen
            title="Prepare-se, a partida vai começar!"
            description="Fique de olho na TV. As rodadas chegam na próxima fase do projeto."
        />
    );
}

function App() {
    return (
        <PlayerProvider>
            <Toaster position="top-center" richColors theme="dark" />
            <Screen />
        </PlayerProvider>
    );
}

export default App;
