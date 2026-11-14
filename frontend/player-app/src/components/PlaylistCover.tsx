interface IPlaylistCover {
    cover: string | null;
    name: string;
    className?: string;
}

/** Capa da playlist, com um quadro neutro quando a Deezer não envia imagem. */
const PlaylistCover = ({ cover, name, className = "" }: IPlaylistCover) =>
    cover ? (
        <img src={cover} alt={name} loading="lazy" className={`aspect-square rounded-lg object-cover ${className}`} />
    ) : (
        <div className={`flex aspect-square items-center justify-center rounded-lg bg-white/10 text-3xl ${className}`}>
            ♪
        </div>
    );

export default PlaylistCover;
