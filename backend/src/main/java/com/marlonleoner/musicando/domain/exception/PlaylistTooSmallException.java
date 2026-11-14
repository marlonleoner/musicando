package com.marlonleoner.musicando.domain.exception;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class PlaylistTooSmallException extends BaseException {

    private static final Integer CODE = HttpStatus.CONFLICT.value();

    public PlaylistTooSmallException(int required, int available) {
        super(String.format("The playlist has %d distinct playable songs, but %d are required", available, required), CODE, ErrorCode.PLAYLIST_TOO_SMALL);
    }
}
