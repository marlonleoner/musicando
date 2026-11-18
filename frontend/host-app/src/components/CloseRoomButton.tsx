import { useState } from "react";

interface ICloseRoomButton {
    onClose: () => void;
}

/** Botão redondo de encerrar a sala, com confirmação no segundo clique (clicar fora cancela). */
const CloseRoomButton = ({ onClose }: ICloseRoomButton) => {
    const [confirming, setConfirming] = useState(false);

    return (
        <button
            type="button"
            className={`flex h-12 items-center justify-center rounded-full bg-red-500 font-bold text-white transition hover:bg-red-600 ${
                confirming ? "px-5 text-sm" : "w-12"
            }`}
            aria-label={confirming ? "Confirmar encerramento da sala" : "Encerrar sala"}
            title="Encerrar sala"
            onClick={() => (confirming ? onClose() : setConfirming(true))}
            onBlur={() => setConfirming(false)}
        >
            {confirming ? (
                "Encerrar a sala?"
            ) : (
                <svg viewBox="0 0 24 24" className="size-6" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <path d="M6 6l12 12M18 6 6 18" strokeLinecap="round" />
                </svg>
            )}
        </button>
    );
};

export default CloseRoomButton;
