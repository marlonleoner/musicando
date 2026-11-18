package com.marlonleoner.musicando.service.game;

import com.marlonleoner.musicando.domain.Match;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.exception.MusicProviderException;
import com.marlonleoner.musicando.domain.music.LobbyMusic;
import com.marlonleoner.musicando.domain.music.MusicTrack;
import com.marlonleoner.musicando.domain.socket.SocketEventType;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.dto.response.LobbyAudioBroadcast;
import com.marlonleoner.musicando.dto.socket.SocketEvent;
import com.marlonleoner.musicando.service.cache.RedisService;
import com.marlonleoner.musicando.service.music.MusicService;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.service.socket.SocketService;
import com.marlonleoner.musicando.util.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * Música de fundo do lobby: algumas músicas aleatórias da playlist escolhida, tocadas somente pela TV. As músicas
 * sorteadas ficam guardadas no Redis para a partida evitar usá-las como resposta. Falhas do provedor só deixam o
 * lobby sem música.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LobbyMusicService {

    public static final int TRACKS = 8;

    private static final String KEY = "@MUSICANDO:LOBBY_MUSIC:%s";

    private static final int TTL_SECONDS = 6 * 60 * 60;

    private final MusicService musicService;

    private final RoomService roomService;

    private final SocketService socketService;

    private final RedisService redisService;

    private final JsonMapper jsonMapper;

    private final RandomGenerator random;

    /**
     * Sorteia novas músicas da playlist e as envia à TV.
     */
    public void refresh(String roomCode, String playlistId) {
        draw(roomCode, playlistId).ifPresent(music -> socketService.sendToHost(roomCode, event(music)));
    }

    /**
     * Envia a música do lobby à TV que acabou de (re)conectar, com a sala aguardando jogadores e uma playlist
     * escolhida. Sorteia as músicas se ainda não houver um sorteio para a playlist atual.
     */
    public void sendCurrent(SocketPrincipal principal) {
        if (!principal.isHost()) {
            return;
        }

        final Optional<String> playlistId = roomService.findByCode(principal.roomCode())
                .filter(Room::isWaiting)
                .map(Room::getCurrentMatch)
                .filter(Match::hasPlaylist)
                .map(Match::getPlaylistId);

        if (playlistId.isEmpty()) {
            return;
        }

        final Optional<LobbyMusic> music = find(principal.roomCode())
                .filter(stored -> playlistId.get().equals(stored.playlistId()))
                .or(() -> draw(principal.roomCode(), playlistId.get()));

        music.ifPresent(found -> socketService.sendToParticipant(principal.participantId(), event(found)));
    }

    /**
     * Músicas que tocaram no lobby da sala, para a partida evitar usá-las como resposta.
     */
    public Set<String> trackIds(String roomCode) {
        return find(roomCode).map(music -> Set.copyOf(music.trackIds())).orElse(Set.of());
    }

    public void clear(String roomCode) {
        redisService.invalidate(key(roomCode));
    }

    private Optional<LobbyMusic> draw(String roomCode, String playlistId) {
        final List<MusicTrack> tracks;

        try {
            tracks = new ArrayList<>(musicService.playlistTracks(playlistId).stream().filter(MusicTrack::playable).toList());
        } catch (MusicProviderException ex) {
            log.warn("Could not fetch the tracks of playlist {} for the lobby", LogSanitizer.sanitize(playlistId));
            clear(roomCode);

            return Optional.empty();
        }

        Collections.shuffle(tracks, random);
        final List<String> trackIds = tracks.stream().limit(TRACKS).map(MusicTrack::id).toList();
        final LobbyMusic music = new LobbyMusic(playlistId, trackIds);
        redisService.set(key(roomCode), jsonMapper.writeValueAsString(music), TTL_SECONDS);

        return Optional.of(music);
    }

    private Optional<LobbyMusic> find(String roomCode) {
        return redisService.get(key(roomCode), String.class).map(json -> jsonMapper.readValue(json, LobbyMusic.class));
    }

    private SocketEvent event(LobbyMusic music) {
        final List<String> previews = new ArrayList<>();

        for (String trackId : music.trackIds()) {
            fetchPreview(trackId).ifPresent(previews::add);
        }

        return SocketEvent.of(SocketEventType.LOBBY_AUDIO, new LobbyAudioBroadcast(previews));
    }

    private Optional<String> fetchPreview(String trackId) {
        try {
            return musicService.findPreview(trackId);
        } catch (MusicProviderException ex) {
            log.warn("Could not fetch the preview of lobby track {}", LogSanitizer.sanitize(trackId));

            return Optional.empty();
        }
    }

    private String key(String roomCode) {
        return String.format(KEY, roomCode);
    }
}
