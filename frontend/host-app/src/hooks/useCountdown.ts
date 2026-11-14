import { useEffect, useState } from "react";

const TICK_MS = 200;

/** Milissegundos que faltam até `endsAt`, atualizados algumas vezes por segundo. */
export const useCountdown = (endsAt: number | null): number => {
    const [now, setNow] = useState(() => Date.now());

    useEffect(() => {
        if (endsAt === null) {
            return;
        }

        const timer = window.setInterval(() => setNow(Date.now()), TICK_MS);

        return () => window.clearInterval(timer);
    }, [endsAt]);

    return endsAt === null ? 0 : Math.max(0, endsAt - now);
};
