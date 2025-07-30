interface IGameCode {
    code: string;
}

const GameCode = ({ code }: IGameCode) => {
    return <span className="font-['Monoton']">{code}</span>;
};

export default GameCode;
