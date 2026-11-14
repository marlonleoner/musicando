import { type FormEvent, useState } from "react";
import Logo from "../components/Logo";
import type { ExitReason } from "../context/contexts";
import { usePlayer } from "../hooks/usePlayer";
import { clearCodeFromUrl, codeFromUrl } from "../utils/env.utils";

const CODE_LENGTH = 6;

const NICKNAME_MIN_LENGTH = 2;

const NICKNAME_MAX_LENGTH = 16;

const EXIT_MESSAGES: Record<Exclude<ExitReason, null>, string> = {
    KICKED: "Você foi removido da sala pelo host.",
    ROOM_CLOSED: "A sala foi encerrada.",
    SESSION_EXPIRED: "Sua vaga nessa sala não existe mais. Entre novamente.",
};

export default function Join() {
    const { join, joining, exitReason, dismissExitReason } = usePlayer();
    const [code, setCode] = useState(codeFromUrl);
    const [nickname, setNickname] = useState("");

    const normalizedNickname = nickname.trim().replace(/\s+/g, " ").toUpperCase();
    const canSubmit = code.length === CODE_LENGTH && normalizedNickname.length >= NICKNAME_MIN_LENGTH && !joining;

    const handleSubmit = (event: FormEvent) => {
        event.preventDefault();
        if (!canSubmit) {
            return;
        }

        clearCodeFromUrl();
        void join(code, normalizedNickname);
    };

    return (
        <form className="flex min-h-screen flex-col items-center justify-center gap-8 px-6" onSubmit={handleSubmit}>
            <Logo />

            {exitReason && (
                <div className="flex w-full max-w-sm items-start justify-between gap-3 rounded-xl bg-white/10 p-4 text-sm">
                    <span>{EXIT_MESSAGES[exitReason]}</span>
                    <button
                        type="button"
                        className="text-white/60"
                        onClick={dismissExitReason}
                        aria-label="Fechar aviso"
                    >
                        ✕
                    </button>
                </div>
            )}

            <label className="flex w-full max-w-sm flex-col gap-2">
                <span className="text-sm uppercase text-white/60">Código da sala</span>
                <input
                    className="rounded-xl bg-white/10 px-4 py-3 text-center font-['Monoton'] text-3xl uppercase tracking-[0.3em] outline-none focus:ring-2 focus:ring-[#926CAE]"
                    value={code}
                    onChange={(event) =>
                        setCode(
                            event.target.value
                                .toUpperCase()
                                .replace(/[^A-Z0-9]/g, "")
                                .slice(0, CODE_LENGTH),
                        )
                    }
                    autoCapitalize="characters"
                    autoComplete="off"
                    placeholder="ABC123"
                />
            </label>

            <label className="flex w-full max-w-sm flex-col gap-2">
                <span className="text-sm uppercase text-white/60">Seu apelido</span>
                <input
                    className="rounded-xl bg-white/10 px-4 py-3 text-xl uppercase outline-none focus:ring-2 focus:ring-[#926CAE]"
                    value={nickname}
                    onChange={(event) => setNickname(event.target.value.slice(0, NICKNAME_MAX_LENGTH))}
                    autoCapitalize="characters"
                    autoComplete="nickname"
                    placeholder="Como quer aparecer na TV"
                />
            </label>

            <button
                type="submit"
                className="w-full max-w-sm rounded-xl bg-[#5F3A90] py-4 text-xl font-bold disabled:opacity-40"
                disabled={!canSubmit}
            >
                {joining ? "Entrando…" : "Entrar"}
            </button>
        </form>
    );
}
