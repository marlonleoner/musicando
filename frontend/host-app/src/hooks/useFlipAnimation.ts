import { type RefObject, useLayoutEffect, useRef } from "react";

const DURATION_MS = 600;
const EASING = "cubic-bezier(0.22, 1, 0.36, 1)";

/**
 * Anima a troca de posição dos filhos de `containerRef` marcados com `data-flip-id` (técnica FLIP): quando `orderKey`
 * muda, cada item que mudou de lugar desliza da posição antiga até a nova. Quem avança passa por cima de quem recua.
 */
export const useFlipAnimation = (containerRef: RefObject<HTMLElement | null>, orderKey: string) => {
    const positions = useRef(new Map<string, number>());

    useLayoutEffect(() => {
        const container = containerRef.current;
        if (!container) {
            return;
        }

        const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
        const items = Array.from(container.querySelectorAll<HTMLElement>("[data-flip-id]"));
        const next = new Map<string, number>();

        // Mede tudo antes de animar, sem animações pela metade atrapalhando a medida.
        items.forEach((item) => item.getAnimations().forEach((animation) => animation.cancel()));
        items.forEach((item) => {
            const id = item.dataset.flipId;
            if (!id) {
                return;
            }

            const left = item.getBoundingClientRect().left;
            const previous = positions.current.get(id);
            next.set(id, left);

            if (reducedMotion || previous === undefined || Math.abs(previous - left) < 1) {
                return;
            }

            item.style.zIndex = previous > left ? "2" : "1";
            const animation = item.animate(
                [{ transform: `translateX(${previous - left}px)` }, { transform: "translateX(0)" }],
                { duration: DURATION_MS, easing: EASING },
            );
            const reset = () => item.style.removeProperty("z-index");
            animation.onfinish = reset;
            animation.oncancel = reset;
        });

        positions.current = next;
    }, [containerRef, orderKey]);
};
