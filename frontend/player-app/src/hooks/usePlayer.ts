import { useContext } from "react";
import { PlayerContext } from "../context/contexts";

export const usePlayer = () => useContext(PlayerContext);
