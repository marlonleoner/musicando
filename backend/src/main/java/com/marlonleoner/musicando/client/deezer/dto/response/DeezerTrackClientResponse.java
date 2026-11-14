package com.marlonleoner.musicando.client.deezer.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeezerTrackClientResponse(
        Long id,
        String title,
        @JsonProperty("title_short") String titleShort,
        Boolean readable,
        String preview,
        DeezerArtistClientResponse artist,
        DeezerErrorClientResponse error) {
}
