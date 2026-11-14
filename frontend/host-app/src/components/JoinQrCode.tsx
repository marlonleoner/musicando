import { QRCodeSVG } from "qrcode.react";
import { isPlayerUrlUnreachable, playerJoinUrl } from "../utils/env.utils";

interface IJoinQrCode {
    code: string;
}

const JoinQrCode = ({ code }: IJoinQrCode) => {
    const url = playerJoinUrl(code);

    return (
        <div className="flex flex-col items-center gap-3">
            <div className="rounded-2xl bg-white p-4">
                <QRCodeSVG value={url} size={220} />
            </div>
            <span className="text-sm text-white/70">{url}</span>
            {isPlayerUrlUnreachable() && (
                <span className="max-w-[260px] text-center text-xs text-amber-300">
                    O link aponta para localhost e não abre no celular. Abra esta tela pelo IP da máquina ou configure
                    VITE_PLAYER_APP_URL.
                </span>
            )}
        </div>
    );
};

export default JoinQrCode;
