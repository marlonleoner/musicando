package com.marlonleoner.musicando.domain.exception;

import com.marlonleoner.musicando.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class MusicProviderException extends BaseException {

    private static final Integer CODE = HttpStatus.SERVICE_UNAVAILABLE.value();

    public MusicProviderException(String message) {
        super(message, CODE, ErrorCode.MUSIC_PROVIDER_UNAVAILABLE);
    }
}
