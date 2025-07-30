import { GameContext } from "@context/GameContext";
import { useTheme } from "@hooks/useTheme";
import { useContext } from "react";
import GameCode from "./GameCode";

function Componente() {
    const { theme, toggleTheme } = useTheme();
    const { room } = useContext(GameContext);

    return (
        <>
            {room && (
                <div>
                    Temos uma sala <GameCode code={room.code} />
                </div>
            )}

            <br />

            <div className="bg-white text-black dark:bg-gray-900 dark:text-white p-4 rounded-md">
                <p>Este é um componente com tema.</p>
                <button onClick={toggleTheme}>Mudar para {theme === "dark" ? "Claro" : "Escuro"}</button>
            </div>
        </>
    );
}

export default Componente;
