import { useEffect, useState } from "react";
import type { Element } from "../types/types";
import { ThemeContext } from "./contexts";

export const ThemeProvider = ({ children }: Element) => {
    const [theme, setThemeState] = useState<"dark" | "light">(() => {
        const savedTheme = localStorage.getItem("@musicando/theme");
        return savedTheme === "light" ? "light" : "dark";
    });

    const toggleTheme = () => {
        setThemeState((prevTheme) => (prevTheme === "dark" ? "light" : "dark"));
    };

    const setTheme = (newTheme: "dark" | "light") => {
        setThemeState(newTheme);
    };

    useEffect(() => {
        const root = window.document.documentElement;

        root.classList.remove("dark", "light");
        root.classList.add(theme);

        localStorage.setItem("@musicando/theme", theme);
    }, [theme]);

    const value = {
        theme,
        toggleTheme,
        setTheme,
    };

    return (
        <ThemeContext.Provider value={value}>
            <div className="flex items-center justify-center">{children}</div>
        </ThemeContext.Provider>
    );
};
