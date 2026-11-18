/** Imagens de cada personagem, geradas por `tools/avatars`: animação parada, comemoração e rosto. */
export type AvatarKind = "idle" | "cheer" | "face";

export const avatarUrl = (avatar: string, kind: AvatarKind): string =>
    `${import.meta.env.BASE_URL}avatars/${avatar}-${kind}.webp`;
