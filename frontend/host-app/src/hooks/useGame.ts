import { useContext } from "react";
import { GameContext } from "../context/contexts";

export const useGame = () => useContext(GameContext);
