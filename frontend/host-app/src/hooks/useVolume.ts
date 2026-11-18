import { useSyncExternalStore } from "react";
import { getVolumeSettings, subscribeVolume } from "../utils/volume.utils";

export const useVolume = () => useSyncExternalStore(subscribeVolume, getVolumeSettings);
