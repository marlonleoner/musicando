package com.marlonleoner.musicando.controller;

import com.marlonleoner.musicando.domain.exception.MusicProviderException;
import com.marlonleoner.musicando.domain.music.MusicPlaylist;
import com.marlonleoner.musicando.handler.ExceptionHandlers;
import com.marlonleoner.musicando.mapper.MusicMapper;
import com.marlonleoner.musicando.service.music.MusicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MusicControllerTest {

    @Mock
    private MusicService musicService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        final MusicMapper mapper = new com.marlonleoner.musicando.mapper.MusicMapperImpl();
        mockMvc = MockMvcBuilders.standaloneSetup(new MusicController(musicService, mapper))
                .setControllerAdvice(new ExceptionHandlers())
                .build();
    }

    @Test
    void shouldReturnOkWhenListingFeaturedPlaylists() throws Exception {
        when(musicService.featuredPlaylists()).thenReturn(List.of(new MusicPlaylist("1", "Hits", "cover.jpg", 10)));

        mockMvc.perform(get("/music/playlists/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].trackCount").value(10));
    }

    @Test
    void shouldReturnServiceUnavailableWhenMusicProviderFails() throws Exception {
        when(musicService.featuredPlaylists()).thenThrow(new MusicProviderException("down"));

        mockMvc.perform(get("/music/playlists/featured"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void shouldReturnOptionsAndDefaultsWhenGettingMatchOptions() throws Exception {
        mockMvc.perform(get("/music/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.defaultRounds").value(10))
                .andExpect(jsonPath("$.defaultRoundDuration").value(20))
                .andExpect(jsonPath("$.rounds.length()").value(4))
                .andExpect(jsonPath("$.roundDurations.length()").value(4));
    }
}
