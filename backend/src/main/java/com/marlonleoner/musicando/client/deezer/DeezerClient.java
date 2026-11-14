package com.marlonleoner.musicando.client.deezer;

import com.marlonleoner.musicando.client.deezer.dto.response.DeezerPageClientResponse;
import com.marlonleoner.musicando.client.deezer.dto.response.DeezerPlaylistClientResponse;
import com.marlonleoner.musicando.client.deezer.dto.response.DeezerTrackClientResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * API pública da Deezer (sem autenticação). Documentação: https://developers.deezer.com/api
 */
@HttpExchange
public interface DeezerClient {

    @GetExchange("/chart/0/playlists")
    DeezerPageClientResponse<DeezerPlaylistClientResponse> chartPlaylists(@RequestParam("limit") int limit);

    @GetExchange("/playlist/{id}")
    DeezerPlaylistClientResponse playlist(@PathVariable("id") String id);

    @GetExchange("/playlist/{id}/tracks")
    DeezerPageClientResponse<DeezerTrackClientResponse> playlistTracks(@PathVariable("id") String id, @RequestParam("index") int index,
            @RequestParam("limit") int limit);
}
