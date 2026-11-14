import Logo from "../components/Logo";

interface IStatusScreen {
    title: string;
    description?: string;
    action?: { label: string; onClick: () => void };
}

export default function StatusScreen({ title, description, action }: IStatusScreen) {
    return (
        <div className="flex min-h-screen flex-col items-center justify-center gap-6 px-6 text-center">
            <Logo />
            <h1 className="text-2xl font-bold">{title}</h1>
            {description && <p className="text-white/70">{description}</p>}
            {action && (
                <button type="button" className="rounded-xl bg-[#5F3A90] px-6 py-3 font-bold" onClick={action.onClick}>
                    {action.label}
                </button>
            )}
        </div>
    );
}
