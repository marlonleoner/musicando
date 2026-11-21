import { useEffect, useState } from "react";
import { usePlayer } from "../hooks/usePlayer";
import { usePlaylistSearch } from "../hooks/usePlaylistSearch";
import type { MatchOptions, MatchSettings, Playlist } from "../types/protocol";
import { getFeaturedPlaylists, getMatchOptions } from "../utils/api.utils";
import OptionChips from "./OptionChips";
import PlaylistCover from "./PlaylistCover";

type Catalog =
    | { status: "loading" }
    | { status: "error" }
    | { status: "ready"; playlists: Playlist[]; options: MatchOptions };

interface IMatchSettingsPanel {
    settings: MatchSettings | null;
}

interface IPlaylistCard {
    playlist: Playlist;
    selected: boolean;
    disabled: boolean;
    onSelect: () => void;
}

const PlaylistCard = ({ playlist, selected, disabled, onSelect }: IPlaylistCard) => (
    <button
        type="button"
        disabled={disabled}
        aria-pressed={selected}
        className={`flex w-28 shrink-0 snap-start flex-col gap-1 rounded-xl p-1.5 text-left transition disabled:opacity-50 ${
            selected ? "bg-amber-400 text-black" : "bg-white/10"
        }`}
        onClick={onSelect}
    >
        <PlaylistCover cover={playlist.cover} name={playlist.name} className="w-full" />
        <span className="line-clamp-2 text-sm font-semibold leading-tight">{playlist.name}</span>
        <span className="text-xs opacity-70">{playlist.trackCount} músicas</span>
    </button>
);

/**
 * Configuração da partida para o VIP: playlist (as do perfil configurado no backend, ou qualquer uma da Deezer pela busca
 * por nome ou por link), quantidade de rodadas, tempo de cada rodada e o botão de iniciar. Cada escolha é enviada ao
 * backend e a tela só muda quando chega o novo estado da sala.
 */
export default function MatchSettingsPanel({ settings }: IMatchSettingsPanel) {
    const { updateSettings, startGame, starting, connection } = usePlayer();
    const [catalog, setCatalog] = useState<Catalog>({ status: "loading" });
    const [attempt, setAttempt] = useState(0);
    const [query, setQuery] = useState("");
    const search = usePlaylistSearch(query);
    const offline = connection !== "connected";

    useEffect(() => {
        let active = true;
        Promise.all([getFeaturedPlaylists(), getMatchOptions()])
            .then(([playlists, options]) => {
                if (active) {
                    setCatalog({ status: "ready", playlists, options });
                }
            })
            .catch(() => {
                if (active) {
                    setCatalog({ status: "error" });
                }
            });

        return () => {
            active = false;
        };
    }, [attempt]);

    const retry = () => {
        setCatalog({ status: "loading" });
        setAttempt((value) => value + 1);
    };

    if (catalog.status === "loading") {
        return <p className="py-6 text-center text-white/60">Carregando playlists…</p>;
    }

    if (catalog.status === "error") {
        return (
            <div className="flex flex-col items-center gap-3 py-6 text-center">
                <p className="text-white/80">Não foi possível carregar as playlists.</p>
                <button type="button" className="rounded-xl bg-white/10 px-5 py-2 font-semibold" onClick={retry}>
                    Tentar novamente
                </button>
            </div>
        );
    }

    const { playlists, options } = catalog;
    const selected = settings?.playlist ?? null;
    const selectedId = selected?.id;
    // A playlist escolhida pela busca não está na lista do perfil: fica fixa no começo enquanto estiver selecionada.
    const mine =
        selected && !playlists.some((playlist) => playlist.id === selected.id) ? [selected, ...playlists] : playlists;
    const searching = search.status !== "idle";
    const shown = searching ? (search.status === "ready" ? search.playlists : []) : mine;
    let notice: string | null = null;
    if (search.status === "loading") {
        notice = "Buscando…";
    } else if (search.status === "error") {
        notice = search.message;
    } else if (shown.length === 0) {
        notice = searching ? "Nenhuma playlist encontrada." : "Nenhuma playlist no perfil. Use a busca acima.";
    }
    const rounds = settings?.rounds ?? options.defaultRounds;
    const roundDuration = settings?.roundDuration ?? options.defaultRoundDuration;

    return (
        <div className="flex flex-col gap-5">
            <section className="flex flex-col gap-2">
                <span className="text-sm uppercase text-white/60">Playlist</span>
                <div className="relative">
                    <input
                        type="search"
                        value={query}
                        onChange={(event) => setQuery(event.target.value)}
                        placeholder="Buscar playlist ou colar link"
                        aria-label="Buscar playlist pelo nome ou colar o link da Deezer"
                        autoComplete="off"
                        enterKeyHint="search"
                        className="w-full rounded-xl bg-white/10 px-4 py-2.5 pr-11 text-base placeholder:text-white/40 focus:outline-none focus:ring-2 focus:ring-amber-400 [&::-webkit-search-cancel-button]:hidden"
                    />
                    {query && (
                        <button
                            type="button"
                            aria-label="Limpar busca"
                            className="absolute right-1.5 top-1/2 flex h-8 w-8 -translate-y-1/2 items-center justify-center rounded-full text-xl text-white/60"
                            onClick={() => setQuery("")}
                        >
                            ×
                        </button>
                    )}
                </div>
                {notice ? (
                    <p className="flex min-h-[11.2rem] items-center justify-center px-4 text-center text-sm text-white/60">
                        {notice}
                    </p>
                ) : (
                    <div className="-mx-4 flex snap-x scroll-px-4 gap-3 overflow-x-auto px-4 pb-2">
                        {shown.map((playlist) => (
                            <PlaylistCard
                                key={playlist.id}
                                playlist={playlist}
                                selected={playlist.id === selectedId}
                                disabled={offline}
                                onSelect={() =>
                                    playlist.id !== selectedId && updateSettings({ playlistId: playlist.id })
                                }
                            />
                        ))}
                    </div>
                )}
            </section>

            <OptionChips
                label="Rodadas"
                options={options.rounds}
                value={rounds}
                disabled={offline}
                onChange={(value) => updateSettings({ rounds: value })}
            />

            <OptionChips
                label="Tempo por rodada"
                options={options.roundDurations}
                value={roundDuration}
                suffix=" s"
                disabled={offline}
                onChange={(value) => updateSettings({ roundDuration: value })}
            />

            <div className="flex flex-col gap-2">
                <button
                    type="button"
                    disabled={!selectedId || starting || offline}
                    className="rounded-xl bg-emerald-500 py-3 text-lg font-bold transition disabled:opacity-40"
                    onClick={startGame}
                >
                    {starting ? "Iniciando…" : "Iniciar partida"}
                </button>
                {!selectedId && (
                    <span className="text-center text-sm text-white/60">Escolha uma playlist para começar.</span>
                )}
            </div>
        </div>
    );
}
