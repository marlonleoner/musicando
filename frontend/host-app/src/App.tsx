import Componente from "@components/Componente";
import { RoomNotCreated } from "@context/FilterContext";
import { GameProvider } from "@context/GameContext";
import { ThemeProvider } from "@context/ThemeContext";
import Home from "./pages/Home";
import { Toaster } from "sonner";

function App() {
    return (
        <>
            <Toaster position="top-right" richColors theme="dark" />
            <ThemeProvider>
                <GameProvider>
                    <RoomNotCreated fallback={Home}>
                        {/* 
                            <GameContainer>
                            <GameFinished fallback={GameOver}>
                            <GameInLobby fallback={Lobby}>
                            <GameLayout />
                            </GameInLobby>
                            </GameFinished>
                            </GameContainer> 
                        */}
                        <Componente />
                    </RoomNotCreated>
                </GameProvider>
            </ThemeProvider>
        </>
    );
}

export default App;
