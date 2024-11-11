import { Element } from "../types/types";

const colors = [
    "#A52A2A", // Brown
    "#7FFFD4", // Aquamarine
    "#D2691E", // Chocolate
    "#FF4500", // OrangeRed
    "#8A2BE2", // BlueViolet
];

export const ThemeProvider = ({ children }: Element) => {
    return (
        <div className="w-screen h-screen flex items-center justify-center text-white bg-black">
            <div className="z-0 fixed top-1/2 left-1/2 w-1 h-1 rounded-full bg-white [box-shadow:-42vw_-4vh_0px_0px_#fff,25vw_-41vh_0px_0px_#fff,-20vw_49vh_0px_1px_#fff,5vw_40vh_1px_1px_#fff,29vw_19vh_1px_0px_#fff,-44vw_-13vh_0px_0px_#fff,46vw_41vh_0px_1px_#fff,-3vw_-45vh_0px_1px_#fff,47vw_35vh_1px_0px_#fff,12vw_-8vh_1px_0px_#fff,-34vw_48vh_1px_1px_#fff,32vw_26vh_1px_1px_#fff,32vw_-41vh_1px_1px_#fff,0vw_37vh_1px_1px_#fff,34vw_-26vh_1px_0px_#fff,-14vw_-49vh_1px_0px_#fff,-12vw_45vh_0px_1px_#fff,-44vw_-33vh_0px_1px_#fff,-13vw_41vh_0px_0px_#fff,-36vw_-11vh_0px_1px_#fff,-23vw_-24vh_1px_0px_#fff,-38vw_-27vh_0px_1px_#fff,16vw_-19vh_0px_0px_#fff,28vw_33vh_1px_0px_#fff,-49vw_-4vh_0px_0px_#fff,16vw_32vh_0px_1px_#fff,36vw_-18vh_1px_0px_#fff,-25vw_-30vh_1px_0px_#fff,-23vw_24vh_0px_1px_#fff,-2vw_-35vh_1px_1px_#fff,-25vw_9vh_0px_0px_#fff,-15vw_-34vh_0px_0px_#fff,-8vw_-19vh_1px_0px_#fff,-20vw_-20vh_1px_1px_#fff,42vw_50vh_0px_1px_#fff,-32vw_10vh_1px_0px_#fff,-23vw_-17vh_0px_0px_#fff,44vw_15vh_1px_0px_#fff,-40vw_33vh_1px_1px_#fff,-43vw_8vh_0px_0px_#fff,-48vw_-15vh_1px_1px_#fff,-24vw_17vh_0px_0px_#fff,-31vw_50vh_1px_0px_#fff,36vw_-38vh_0px_1px_#fff,-7vw_48vh_0px_0px_#fff,15vw_-32vh_0px_0px_#fff,29vw_-41vh_0px_0px_#fff,2vw_37vh_1px_0px_#fff,7vw_-40vh_1px_1px_#fff,15vw_18vh_0px_0px_#fff,25vw_-13vh_1px_1px_#fff,-46vw_-12vh_1px_1px_#fff,-18vw_22vh_0px_0px_#fff,23vw_-9vh_1px_0px_#fff,50vw_12vh_0px_1px_#fff,45vw_2vh_0px_0px_#fff,14vw_-48vh_1px_0px_#fff,23vw_43vh_0px_1px_#fff,-40vw_16vh_1px_1px_#fff,20vw_-31vh_0px_1px_#fff,-17vw_44vh_1px_1px_#fff,18vw_-45vh_0px_0px_#fff,33vw_-6vh_0px_0px_#fff,0vw_7vh_0px_1px_#fff,-10vw_-18vh_0px_1px_#fff,-19vw_5vh_1px_0px_#fff,1vw_42vh_0px_0px_#fff,22vw_48vh_0px_1px_#fff,39vw_-8vh_1px_1px_#fff,-6vw_-42vh_1px_0px_#fff,-47vw_34vh_0px_0px_#fff,-46vw_19vh_0px_1px_#fff,-12vw_-32vh_0px_0px_#fff,-45vw_-38vh_0px_1px_#fff,-28vw_18vh_1px_0px_#fff,-38vw_-46vh_1px_1px_#fff,49vw_-6vh_1px_1px_#fff,-28vw_18vh_1px_1px_#fff,10vw_-24vh_0px_1px_#fff,-5vw_-11vh_1px_1px_#fff,33vw_-8vh_1px_0px_#fff,-16vw_17vh_0px_0px_#fff,18vw_27vh_0px_1px_#fff,-8vw_-10vh_1px_1px_#fff] [box-shadow:24vw_9vh_1px_0px_#fff,12vw_-24vh_0px_1px_#fff,-45vw_-22vh_0px_0px_#fff,-37vw_-40vh_0px_1px_#fff,29vw_19vh_0px_1px_#fff,4vw_-8vh_0px_1px_#fff,-5vw_21vh_1px_1px_#fff,-27vw_26vh_1px_1px_#fff,-47vw_-3vh_1px_1px_#fff,-28vw_-30vh_0px_1px_#fff,-43vw_-27vh_0px_1px_#fff,4vw_22vh_1px_1px_#fff,36vw_23vh_0px_0px_#fff,-21vw_24vh_1px_1px_#fff,-16vw_2vh_1px_0px_#fff,-16vw_-6vh_0px_0px_#fff,5vw_26vh_0px_0px_#fff,-34vw_41vh_0px_0px_#fff,1vw_42vh_1px_1px_#fff,11vw_-13vh_1px_1px_#fff,48vw_-8vh_1px_0px_#fff,22vw_-15vh_0px_0px_#fff,45vw_49vh_0px_0px_#fff,43vw_-27vh_1px_1px_#fff,20vw_-2vh_0px_0px_#fff,8vw_22vh_0px_1px_#fff,39vw_48vh_1px_1px_#fff,-21vw_-11vh_0px_1px_#fff,-40vw_45vh_0px_1px_#fff,11vw_-30vh_1px_0px_#fff,26vw_30vh_1px_0px_#fff,45vw_-29vh_0px_1px_#fff,-2vw_18vh_0px_0px_#fff,-29vw_-45vh_1px_0px_#fff,-7vw_-27vh_1px_1px_#fff,42vw_24vh_0px_0px_#fff,45vw_-48vh_1px_0px_#fff,-36vw_-18vh_0px_0px_#fff,-44vw_13vh_0px_1px_#fff,36vw_16vh_0px_1px_#fff,40vw_24vh_0px_0px_#fff,18vw_11vh_0px_0px_#fff,-15vw_-23vh_1px_0px_#fff,-24vw_48vh_0px_1px_#fff,27vw_-45vh_1px_0px_#fff,-2vw_-24vh_0px_1px_#fff,-15vw_-28vh_0px_0px_#fff,-43vw_13vh_1px_0px_#fff,7vw_27vh_1px_0px_#fff,47vw_5vh_0px_0px_#fff,-45vw_15vh_1px_1px_#fff,-5vw_-28vh_0px_1px_#fff,38vw_25vh_1px_1px_#fff,-39vw_-1vh_1px_0px_#fff,5vw_0vh_1px_0px_#fff,49vw_13vh_0px_0px_#fff,48vw_10vh_0px_1px_#fff,19vw_-28vh_0px_0px_#fff,4vw_7vh_0px_0px_#fff,21vw_21vh_1px_1px_#fff,-15vw_-15vh_0px_1px_#fff,-6vw_-42vh_1px_0px_#fff,-15vw_48vh_1px_1px_#fff,-23vw_25vh_1px_1px_#fff,-48vw_25vh_0px_1px_#fff,-31vw_-19vh_0px_1px_#fff,4vw_37vh_1px_1px_#fff,-43vw_28vh_0px_0px_#fff,3vw_-25vh_0px_1px_#fff,-39vw_14vh_0px_1px_#fff,-40vw_31vh_0px_1px_#fff,35vw_-36vh_1px_1px_#fff,16vw_49vh_0px_0px_#fff,6vw_39vh_0px_0px_#fff,3vw_-35vh_0px_1px_#fff,-44vw_-2vh_1px_0px_#fff,-6vw_21vh_1px_0px_#fff,48vw_9vh_1px_1px_#fff,-43vw_30vh_1px_1px_#fff,29vw_-12vh_1px_1px_#fff,-48vw_13vh_1px_0px_#fff,-42vw_32vh_1px_1px_#fff,34vw_15vh_1px_1px_#fff,29vw_-37vh_1px_1px_#fff,28vw_2vh_0px_0px_#fff] animate-zoom"></div>
            {children}
        </div>
    );
};
