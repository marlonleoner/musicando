package com.marlonleoner.musicando.domain.music;

/**
 * Música de um provedor de músicas.
 *
 * @param id       identificador no provedor
 * @param title    título, sem a indicação de versão quando o provedor a separa
 * @param artist   nome do artista
 * @param playable indica se a música tem prévia disponível para tocar
 */
public record MusicTrack(String id, String title, String artist, boolean playable) {
}
