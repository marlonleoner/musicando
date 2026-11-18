/** Imagens de cada personagem, geradas por `tools/avatars`: animação parada, comemoração e rosto. */
export type AvatarKind = "idle" | "cheer" | "face";

export const avatarUrl = (avatar: string, kind: AvatarKind): string =>
    `${import.meta.env.BASE_URL}avatars/${avatar}-${kind}.webp`;

/** Personagens disponíveis. A mesma lista existe no backend (`AvatarValidator`) e em `tools/avatars`. */
export const AVATARS = [
    "alien",
    "female-a",
    "female-b",
    "female-c",
    "female-d",
    "female-e",
    "female-f",
    "knight",
    "male-a",
    "male-b",
    "male-c",
    "male-d",
    "male-e",
    "male-f",
    "male-g",
    "raider",
    "robot",
    "soldier",
    "worker",
    "zombie",
] as const;
