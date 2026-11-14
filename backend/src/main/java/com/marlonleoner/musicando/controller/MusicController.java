package com.marlonleoner.musicando.controller;

import com.marlonleoner.musicando.domain.exception.MusicProviderException;
import com.marlonleoner.musicando.dto.response.MatchOptionsResponse;
import com.marlonleoner.musicando.dto.response.PlaylistResponse;
import com.marlonleoner.musicando.mapper.MusicMapper;
import com.marlonleoner.musicando.service.music.MusicService;
import com.marlonleoner.musicando.validator.MatchSettingsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/music")
@RequiredArgsConstructor
public class MusicController {

    private final MusicService musicService;

    private final MusicMapper musicMapper;

    @GetMapping("/playlists/featured")
    public ResponseEntity<List<PlaylistResponse>> getFeaturedPlaylists() throws MusicProviderException {
        return ResponseEntity.ok(musicMapper.toResponse(musicService.featuredPlaylists()));
    }

    @GetMapping("/options")
    public ResponseEntity<MatchOptionsResponse> getMatchOptions() {
        return ResponseEntity.ok(new MatchOptionsResponse(MatchSettingsValidator.ROUND_OPTIONS, MatchSettingsValidator.ROUND_DURATION_OPTIONS,
                MatchSettingsValidator.DEFAULT_ROUNDS, MatchSettingsValidator.DEFAULT_ROUND_DURATION));
    }
}
