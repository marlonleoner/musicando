interface IPositionChange {
    change: number;
    className?: string;
}

/** Seta indicando quantas posições o jogador subiu ou desceu no placar. */
const PositionChange = ({ change, className = "" }: IPositionChange) => {
    if (change === 0) {
        return <span className={`w-12 text-center text-white/30 ${className}`}>–</span>;
    }

    return (
        <span className={`w-12 text-center font-bold ${change > 0 ? "text-emerald-400" : "text-red-400"} ${className}`}>
            {change > 0 ? `▲${change}` : `▼${-change}`}
        </span>
    );
};

export default PositionChange;
