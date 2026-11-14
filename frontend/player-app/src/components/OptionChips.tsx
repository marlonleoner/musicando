interface IOptionChips {
    label: string;
    options: number[];
    value?: number;
    suffix?: string;
    disabled?: boolean;
    onChange: (value: number) => void;
}

/** Linha de botões para escolher um valor entre poucas opções. */
const OptionChips = ({ label, options, value, suffix = "", disabled = false, onChange }: IOptionChips) => (
    <div className="flex flex-col gap-2">
        <span className="text-sm uppercase text-white/60">{label}</span>
        <div className="flex gap-2">
            {options.map((option) => (
                <button
                    key={option}
                    type="button"
                    disabled={disabled}
                    aria-pressed={option === value}
                    className={`grow rounded-lg py-2 font-semibold transition disabled:opacity-50 ${
                        option === value ? "bg-amber-400 text-black" : "bg-white/10 text-white"
                    }`}
                    onClick={() => option !== value && onChange(option)}
                >
                    {option}
                    {suffix}
                </button>
            ))}
        </div>
    </div>
);

export default OptionChips;
