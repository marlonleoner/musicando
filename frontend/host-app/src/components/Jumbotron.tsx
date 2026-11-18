import Logo from "./Logo";

interface IJumbotron {
    remainingMs: number;
    totalMs: number;
}

/** Letreiro no topo da partida: o logo e, na base, a barra com o tempo que falta na etapa atual. */
const Jumbotron = ({ remainingMs, totalMs }: IJumbotron) => {
    const percent = totalMs > 0 ? Math.min(100, (remainingMs / totalMs) * 100) : 0;

    return (
        <div className="absolute left-1/2 top-0 z-10 w-[min(640px,48vw)] -translate-x-1/2">
            <div className="flex flex-col items-center gap-4 bg-[#2e2049] px-[12%] pb-5 pt-4 [clip-path:polygon(0_0,100%_0,92%_100%,8%_100%)]">
                <Logo size="small" />
                <div className="h-3 w-full overflow-hidden rounded-full bg-black/40 2xl:h-4">
                    <div
                        className={`h-full rounded-full transition-[width] duration-200 ease-linear ${percent < 25 ? "bg-red-500" : "bg-amber-400"}`}
                        style={{ width: `${percent}%` }}
                    />
                </div>
            </div>
        </div>
    );
};

export default Jumbotron;
