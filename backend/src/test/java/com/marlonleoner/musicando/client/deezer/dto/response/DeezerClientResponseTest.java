package com.marlonleoner.musicando.client.deezer.dto.response;

import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class DeezerClientResponseTest {

    private final JsonMapper mapper = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();

    @Test
    void shouldReadSnakeCaseFieldsWhenDeserializingPlaylist() {
        final String json = """
                {"id":908622995,"title":"Top Brasil","nb_tracks":80,"picture_medium":"cover.jpg","public":true,"is_loved_track":false,
                 "creator":{"id":2037081106,"name":"Marlon","type":"user"}}
                """;

        final DeezerPlaylistClientResponse playlist = mapper.readValue(json, DeezerPlaylistClientResponse.class);

        assertThat(playlist).isEqualTo(new DeezerPlaylistClientResponse(908622995L, "Top Brasil", 80, "cover.jpg", false,
                new DeezerUserClientResponse(2037081106L), null));
    }

    @Test
    void shouldReadTracksAndArtistWhenDeserializingTracksPage() {
        final String json = """
                {"data":[{"id":1,"title":"Song (Live)","title_short":"Song","readable":true,"preview":"p.mp3","artist":{"id":5,"name":"Artist"},
                 "album":{"id":7,"cover_medium":"m.jpg","cover_big":"b.jpg"}}],
                 "total":1,"next":"https://api.deezer.com/next"}
                """;

        final DeezerPageClientResponse<DeezerTrackClientResponse> page = mapper.readValue(json, new TypeReference<>() { });

        assertThat(page.data()).containsExactly(
                new DeezerTrackClientResponse(1L, "Song (Live)", "Song", true, "p.mp3", new DeezerArtistClientResponse("Artist"),
                        new DeezerAlbumClientResponse("m.jpg", "b.jpg"), null));
        assertThat(page.next()).isEqualTo("https://api.deezer.com/next");
        assertThat(page.error()).isNull();
    }

    @Test
    void shouldReadErrorAndEmptyDataWhenDeserializingErrorPage() {
        final String json = """
                {"error":{"type":"DataException","message":"no data","code":800}}
                """;

        final DeezerPageClientResponse<DeezerTrackClientResponse> page = mapper.readValue(json, new TypeReference<>() { });

        assertThat(page.data()).isEmpty();
        assertThat(page.error()).isEqualTo(new DeezerErrorClientResponse("DataException", "no data", 800));
    }
}
