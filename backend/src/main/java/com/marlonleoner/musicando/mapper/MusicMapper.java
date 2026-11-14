package com.marlonleoner.musicando.mapper;

import com.marlonleoner.musicando.domain.music.MusicPlaylist;
import com.marlonleoner.musicando.dto.response.PlaylistResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface MusicMapper {

    PlaylistResponse toResponse(MusicPlaylist playlist);

    List<PlaylistResponse> toResponse(List<MusicPlaylist> playlists);
}
