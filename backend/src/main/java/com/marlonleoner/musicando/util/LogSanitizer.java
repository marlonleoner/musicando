package com.marlonleoner.musicando.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Remove quebras de linha de valores externos antes de registrá-los em log, evitando a injeção de linhas falsas
 * (CRLF injection).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogSanitizer {

    public static String sanitize(final Object value) {
        if (Objects.isNull(value)) {
            return null;
        }

        return String.valueOf(value).replace('\n', '_').replace('\r', '_');
    }
}
