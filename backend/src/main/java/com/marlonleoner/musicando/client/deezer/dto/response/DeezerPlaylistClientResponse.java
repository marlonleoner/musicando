package com.marlonleoner.musicando.client.deezer.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeezerPlaylistClientResponse(
        Long id,
        String title,
        @JsonProperty("nb_tracks") Integer nbTracks,
        @JsonProperty("picture_medium") String pictureMedium,
        DeezerErrorClientResponse error) {
}
