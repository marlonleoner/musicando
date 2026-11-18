import Avatar from "./Avatar";

interface IAvatarFrame {
    avatar: string | null;
    /** Classes de tamanho da moldura (ex.: `h-12 w-12`). */
    className?: string;
}

/** Rosto do personagem dentro de uma moldura redonda. */
const AvatarFrame = ({ avatar, className = "h-12 w-12" }: IAvatarFrame) => (
    <span
        className={`flex shrink-0 items-end justify-center overflow-hidden rounded-full bg-white/15 ring-2 ring-white/25 ${className}`}
    >
        <Avatar avatar={avatar} kind="face" className="h-[90%] w-[90%]" />
    </span>
);

export default AvatarFrame;
