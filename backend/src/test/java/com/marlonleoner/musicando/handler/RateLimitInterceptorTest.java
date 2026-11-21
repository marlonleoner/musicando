package com.marlonleoner.musicando.handler;

import com.marlonleoner.musicando.config.properties.RateLimitProperties;
import com.marlonleoner.musicando.domain.exception.TooManyRequestsException;
import com.marlonleoner.musicando.service.ratelimit.RateLimiter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateLimitInterceptorTest {

    private final RateLimitInterceptor interceptor = new RateLimitInterceptor(new RateLimiter(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)),
            new RateLimitProperties(2, 3, 20, 2));

    private static MockHttpServletRequest request(final String method, final String path, final String ip) {
        final MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRemoteAddr(ip);

        return request;
    }

    private boolean handle(final MockHttpServletRequest request) throws TooManyRequestsException {
        return interceptor.preHandle(request, new MockHttpServletResponse(), new Object());
    }

    @Test
    void shouldRejectRoomCreationWhenIpExceedsLimitPerMinute() throws TooManyRequestsException {
        assertThat(handle(request("POST", "/room", "10.0.0.1"))).isTrue();
        assertThat(handle(request("POST", "/room", "10.0.0.1"))).isTrue();

        assertThatThrownBy(() -> handle(request("POST", "/room", "10.0.0.1"))).isInstanceOf(TooManyRequestsException.class);
        assertThat(handle(request("POST", "/room", "10.0.0.2"))).isTrue();
    }

    @Test
    void shouldRejectJoinAttemptsWhenIpExceedsLimitPerMinute() throws TooManyRequestsException {
        for (int i = 0; i < 3; i++) {
            assertThat(handle(request("POST", "/room/join", "10.0.0.1"))).isTrue();
        }

        assertThatThrownBy(() -> handle(request("POST", "/room/join", "10.0.0.1"))).isInstanceOf(TooManyRequestsException.class);
        assertThat(handle(request("POST", "/room", "10.0.0.1"))).isTrue();
    }

    @Test
    void shouldIgnoreContextPathWhenMatchingPath() throws TooManyRequestsException {
        final MockHttpServletRequest first = request("POST", "/api/room", "10.0.0.1");
        first.setContextPath("/api");
        handle(first);
        handle(first);

        assertThatThrownBy(() -> handle(first)).isInstanceOf(TooManyRequestsException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "OPTIONS"})
    void shouldNotCountWhenMethodIsNotPost(final String method) throws TooManyRequestsException {
        for (int i = 0; i < 5; i++) {
            assertThat(handle(request(method, "/room", "10.0.0.1"))).isTrue();
        }
    }

    @Test
    void shouldRejectMusicSearchesWhenIpExceedsLimitPerMinute() throws TooManyRequestsException {
        assertThat(handle(request("GET", "/music/playlists/search", "10.0.0.1"))).isTrue();
        assertThat(handle(request("GET", "/music/playlists/resolve", "10.0.0.1"))).isTrue();

        assertThatThrownBy(() -> handle(request("GET", "/music/playlists/search", "10.0.0.1"))).isInstanceOf(TooManyRequestsException.class);
        assertThat(handle(request("GET", "/music/playlists/search", "10.0.0.2"))).isTrue();
        assertThat(handle(request("POST", "/room", "10.0.0.1"))).isTrue();
    }

    @Test
    void shouldNotCountMusicSearchesWhenMethodIsNotGet() throws TooManyRequestsException {
        for (int i = 0; i < 5; i++) {
            assertThat(handle(request("OPTIONS", "/music/playlists/search", "10.0.0.1"))).isTrue();
        }
    }
}
