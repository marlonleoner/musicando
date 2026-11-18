import { useEffect, useState } from "react";

const TICK_MS = 200;

/**
 * Milissegundos que faltam até `endsAt`, atualizados algumas vezes por segundo. Nunca passa de `durationMs`: logo que
 * uma etapa começa, o relógio guardado ainda é o da atualização anterior, o que faria a contagem começar acima da
 * duração (um "4" numa contagem de 3 segundos).
 */
export const useCountdown = (endsAt: number | null, durationMs = Number.POSITIVE_INFINITY): number => {
    const [now, setNow] = useState(() => Date.now());

    useEffect(() => {
        if (endsAt === null) {
            return;
        }

        const timer = window.setInterval(() => setNow(Date.now()), TICK_MS);

        return () => window.clearInterval(timer);
    }, [endsAt]);

    return endsAt === null ? 0 : Math.min(durationMs, Math.max(0, endsAt - now));
};
