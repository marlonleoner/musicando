import type { JSX } from "react";

export type Element = {
    children: JSX.Element | JSX.Element[] | string;
};

export type FallbackElement = Element & {
    fallback: () => JSX.Element;
};

export interface IRoom {
    id: string;
    code: string;
    secret: string;
}

export interface IPlayer {
    id: string;
    name: string;
    score: number;
}

export interface IGame {
    playlistId: string;
    numberOfSongs: number;
    timePerRound: number;
}

export interface RoundState {
    currentRound: number;
    songTitle: string;
}

export interface IGameContext {
    room?: IRoom;
    players: IPlayer[];

    createRoom: () => Promise<void>;
    joinPlayer: (player: IPlayer) => void;
}
