package com.marlonleoner.musicando.dto.response;

import java.util.List;

/**
 * Prévias que a TV toca de fundo no lobby, uma depois da outra.
 */
public record LobbyAudioBroadcast(List<String> previewUrls) {
    public LobbyAudioBroadcast {
        previewUrls = List.copyOf(previewUrls);
    }
}
