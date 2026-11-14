interface IGameCode {
    code: string;
}

const GameCode = ({ code }: IGameCode) => {
    return <span className="font-['Monoton'] tracking-widest">{code}</span>;
};

export default GameCode;
