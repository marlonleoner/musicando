import Logo from "../components/Logo";

interface IStatusScreen {
    title: string;
    description?: string;
    action?: { label: string; onClick: () => void };
}

export default function StatusScreen({ title, description, action }: IStatusScreen) {
    return (
        <div className="flex min-h-screen grow flex-col items-center justify-center gap-10 p-10 text-center">
            <Logo size="small" />
            <h1 className="text-4xl font-bold text-white">{title}</h1>
            {description && <p className="max-w-xl text-xl text-white/70">{description}</p>}
            {action && (
                <button
                    type="button"
                    className="rounded-xl bg-[#5F3A90] px-6 py-3 text-xl font-bold text-white"
                    onClick={action.onClick}
                >
                    {action.label}
                </button>
            )}
        </div>
    );
}
