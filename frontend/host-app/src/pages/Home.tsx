import Logo from "../components/Logo";
import { useGame } from "../hooks/useGame";

export default function Home() {
    const { createRoom, creatingRoom } = useGame();

    return (
        <div className="flex min-h-screen grow flex-col items-center justify-center gap-[30vh]">
            <Logo />
            <button
                type="button"
                className="font-['Monoton'] text-[2.4vw] uppercase tracking-[-0.1rem] text-white disabled:opacity-50"
                onClick={createRoom}
                disabled={creatingRoom}
            >
                {creatingRoom ? "Criando…" : "Criar Sala"}
            </button>
        </div>
    );
}
