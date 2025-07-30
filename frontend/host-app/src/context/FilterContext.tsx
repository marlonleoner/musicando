import { useContext, useMemo } from "react";
import { GameContext } from "./GameContext";
import type { FallbackElement, IGameContext } from "src/types/types";

const FilterContext = (
    filter: (context: IGameContext) => boolean,
    { children, fallback: Fallback }: FallbackElement,
) => {
    const context = useContext(GameContext);
    const condition = useMemo(() => filter(context), [context]);

    return <>{condition ? <Fallback /> : children}</>;
};

/**
 * Componente de fallback condicional que renderiza os elementos fornecidos
 * somente se **nenhuma sala (`room`) tiver sido criada** no contexto atual do jogo.
 *
 * Utiliza o `FilterContext` para acessar o contexto `IGameContext` e verifica
 * se a propriedade `room` está ausente ou indefinida.
 *
 * @param fallbackElement - Elementos a serem renderizados quando não houver sala criada.
 * @returns JSX que renderiza os elementos de fallback caso `room` seja falsy.
 */
export const RoomNotCreated = (fallbackElement: FallbackElement) =>
    FilterContext(({ room }: IGameContext) => !room, fallbackElement);
