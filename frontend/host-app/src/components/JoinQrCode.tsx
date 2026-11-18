import { QRCodeSVG } from "qrcode.react";
import { isPlayerUrlUnreachable, playerJoinUrl } from "../utils/env.utils";

interface IJoinQrCode {
    code: string;
    size?: number;
}

/** Endereço do app do celular sem o protocolo e sem o código, para ser digitado. */
const displayHost = (url: string): string => {
    try {
        return new URL(url).host;
    } catch {
        return url;
    }
};

const JoinQrCode = ({ code, size = 160 }: IJoinQrCode) => {
    const url = playerJoinUrl(code);

    return (
        <div className="flex flex-col items-center gap-2">
            <span className="text-lg font-semibold text-white">Aponte a câmera para entrar</span>
            <div className="rounded-2xl bg-white p-3">
                <QRCodeSVG value={url} size={size} />
            </div>
            <span className="text-sm text-white/70">{displayHost(url)}</span>
            {isPlayerUrlUnreachable() && (
                <span className="max-w-[240px] text-center text-xs text-amber-300">
                    O link aponta para localhost e não abre no celular. Abra esta tela pelo IP da máquina ou configure
                    VITE_PLAYER_APP_URL.
                </span>
            )}
        </div>
    );
};

export default JoinQrCode;
