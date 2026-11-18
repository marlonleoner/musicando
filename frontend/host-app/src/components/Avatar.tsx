import { avatarUrl, type AvatarKind } from "../utils/avatar.utils";

interface IAvatar {
    avatar: string | null;
    kind: AvatarKind;
    className?: string;
    alt?: string;
}

/** Imagem do personagem do jogador. Jogadores sem personagem aparecem com um marcador neutro. */
const Avatar = ({ avatar, kind, className = "", alt = "" }: IAvatar) =>
    avatar ? (
        <img src={avatarUrl(avatar, kind)} alt={alt} className={`object-contain ${className}`} draggable={false} />
    ) : (
        <div className={`flex items-center justify-center rounded-full bg-white/10 text-white/40 ${className}`}>?</div>
    );

export default Avatar;
