import type { ReactNode } from "react";
import Logo from "./Logo";

interface IJumbotron {
    remainingMs: number;
    totalMs: number;
    /** Conteúdo fixo logo abaixo do letreiro (o progresso das rodadas). */
    children?: ReactNode;
}

/** Letreiro no topo da partida: o logo e, colada na base, a barra com o tempo que falta na etapa atual. */
const Jumbotron = ({ remainingMs, totalMs, children }: IJumbotron) => {
    const percent = totalMs > 0 ? Math.min(100, (remainingMs / totalMs) * 100) : 0;

    return (
        <div className="absolute left-1/2 top-0 z-30 flex w-[min(820px,58vw)] -translate-x-1/2 flex-col items-center gap-[1vh]">
            <div className="relative flex w-full justify-center bg-[#2e2049] pb-[2.4vh] pt-[0.6vh] [clip-path:polygon(0_0,100%_0,94%_100%,6%_100%)]">
                <Logo size="small" />
                <div className="absolute inset-x-0 bottom-0 h-[clamp(0.375rem,1.3vh,1rem)] bg-black/40">
                    <div
                        className={`h-full transition-[width] duration-200 ease-linear ${percent < 25 ? "bg-red-500" : "bg-amber-400"}`}
                        style={{ width: `${percent}%` }}
                    />
                </div>
            </div>
            {children}
        </div>
    );
};

export default Jumbotron;
