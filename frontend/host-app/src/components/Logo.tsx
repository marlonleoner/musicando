interface ILogo {
    size?: "large" | "small";
}

const Logo = ({ size = "large" }: ILogo) => (
    <div
        className={`font-['Monoton'] text-white ${
            size === "large"
                ? "text-[8vw] tracking-[-0.4vw] [text-shadow:-4px_4px_5px_#44277B,-10px_10px_5px_#5F3A90,-18px_18px_5px_#926CAE]"
                : "text-[3vw] tracking-[-0.15vw] [text-shadow:-2px_2px_4px_#44277B,-5px_5px_4px_#5F3A90]"
        }`}
    >
        MUSICANDO
    </div>
);

export default Logo;
