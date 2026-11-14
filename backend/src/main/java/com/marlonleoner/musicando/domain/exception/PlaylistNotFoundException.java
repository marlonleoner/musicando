package com.marlonleoner.musicando.domain.exception;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class PlaylistNotFoundException extends BaseException {

    private static final Integer CODE = HttpStatus.NOT_FOUND.value();

    public PlaylistNotFoundException(String playlistId) {
        super(String.format("Playlist %s not found", playlistId), CODE, ErrorCode.PLAYLIST_NOT_FOUND);
    }
}
