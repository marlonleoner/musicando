package com.marlonleoner.musicando.client.deezer.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeezerAlbumClientResponse(
        @JsonProperty("cover_medium") String coverMedium,
        @JsonProperty("cover_big") String coverBig) {
}
