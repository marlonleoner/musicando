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

const JoinQrCode = ({ code, size = 256 }: IJoinQrCode) => {
    const url = playerJoinUrl(code);

    return (
        <div className="flex flex-col items-center gap-[0.8vh]">
            <span className="text-[clamp(0.75rem,2.4vh,1.25rem)] font-semibold text-white">
                Aponte a câmera para entrar
            </span>
            {/* O QR acompanha a altura da tela, para caber também em TVs que simulam uma tela menor. */}
            <div className="h-[clamp(6rem,24vh,13rem)] w-[clamp(6rem,24vh,13rem)] rounded-2xl bg-white p-[1.2vh]">
                <QRCodeSVG value={url} size={size} className="h-full w-full" />
            </div>
            <span className="text-[clamp(0.625rem,1.8vh,0.875rem)] text-white/70">{displayHost(url)}</span>
            {isPlayerUrlUnreachable() && (
                <span className="max-w-[30vh] text-center text-[clamp(0.5rem,1.4vh,0.75rem)] text-amber-300">
                    O link aponta para localhost e não abre no celular. Abra esta tela pelo IP da máquina ou configure
                    VITE_PLAYER_APP_URL.
                </span>
            )}
        </div>
    );
};

export default JoinQrCode;
