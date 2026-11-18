import { AVATARS } from "../utils/avatar.utils";
import Avatar from "./Avatar";

interface IAvatarPicker {
    current: string | null;
    /** Personagens em uso por outros jogadores da sala. */
    taken: ReadonlySet<string>;
    onSelect: (avatar: string) => void;
}

/** Grade com todos os personagens. Os que outros jogadores já escolheram ficam apagados e não podem ser escolhidos. */
const AvatarPicker = ({ current, taken, onSelect }: IAvatarPicker) => (
    <ul className="grid w-full grid-cols-5 gap-2">
        {AVATARS.map((avatar) => {
            const unavailable = taken.has(avatar);
            const selected = avatar === current;

            return (
                <li key={avatar}>
                    <button
                        type="button"
                        className={`aspect-square w-full rounded-xl border-2 bg-white/10 p-1 transition ${
                            selected ? "border-amber-400" : "border-transparent"
                        } ${unavailable ? "opacity-30" : "active:scale-95"}`}
                        disabled={unavailable}
                        aria-pressed={selected}
                        aria-label={unavailable ? `${avatar} (em uso)` : avatar}
                        onClick={() => onSelect(avatar)}
                    >
                        <Avatar avatar={avatar} kind="face" className="h-full w-full" />
                    </button>
                </li>
            );
        })}
    </ul>
);

export default AvatarPicker;
