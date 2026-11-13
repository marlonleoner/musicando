package com.marlonleoner.musicando.handler;

import com.marlonleoner.musicando.domain.exception.BaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionHandlersTest {

    private final ExceptionHandlers handlers = new ExceptionHandlers();

    @ParameterizedTest
    @ValueSource(ints = {400, 404, 409, 500})
    void shouldUseExceptionCodeAsStatusWhenHandlingBaseException(final int code) {
        final BaseException exception = new BaseException("business error", code);

        assertThat(handlers.handleBaseException(exception).getStatusCode().value()).isEqualTo(code);
    }

    @Test
    void shouldReturnInternalServerErrorWhenHandlingUnexpectedException() {
        final Exception exception = new IllegalStateException("database password is 123");

        assertThat(handlers.handleGenericException(exception).getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldNotExposeInternalMessageWhenHandlingUnexpectedException() {
        final Exception exception = new IllegalStateException("database password is 123");

        assertThat(handlers.handleGenericException(exception).getBody().message()).doesNotContain("password");
    }
}
