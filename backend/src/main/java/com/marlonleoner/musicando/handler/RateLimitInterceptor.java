package com.marlonleoner.musicando.handler;

import com.marlonleoner.musicando.config.properties.RateLimitProperties;
import com.marlonleoner.musicando.domain.exception.TooManyRequestsException;
import com.marlonleoner.musicando.service.ratelimit.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

/**
 * Limita, por IP, a criação de salas, as tentativas de entrar em uma sala (o que também dificulta adivinhar códigos) e as buscas
 * de playlist, que viram chamadas à Deezer.
 */
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    public static final String CREATE_ROOM_PATH = "/room";

    public static final String JOIN_ROOM_PATH = "/room/join";

    public static final String SEARCH_PLAYLISTS_PATH = "/music/playlists/search";

    public static final String RESOLVE_PLAYLIST_PATH = "/music/playlists/resolve";

    private static final Duration ONE_MINUTE = Duration.ofMinutes(1);

    private final RateLimiter rateLimiter;

    private final RateLimitProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws TooManyRequestsException {
        final String path = request.getRequestURI().substring(request.getContextPath().length());
        final String ip = request.getRemoteAddr();

        if (HttpMethod.GET.matches(request.getMethod()) && (SEARCH_PLAYLISTS_PATH.equals(path) || RESOLVE_PLAYLIST_PATH.equals(path))) {
            if (!rateLimiter.tryAcquire("music-search:" + ip, properties.musicSearchesPerMinute(), ONE_MINUTE)) {
                throw new TooManyRequestsException();
            }

            return true;
        }

        if (!HttpMethod.POST.matches(request.getMethod())) {
            return true;
        }

        if (CREATE_ROOM_PATH.equals(path) && !rateLimiter.tryAcquire("room-create:" + ip, properties.roomCreationsPerMinute(), ONE_MINUTE)) {
            throw new TooManyRequestsException();
        }

        if (JOIN_ROOM_PATH.equals(path) && !rateLimiter.tryAcquire("room-join:" + ip, properties.roomJoinsPerMinute(), ONE_MINUTE)) {
            throw new TooManyRequestsException();
        }

        return true;
    }
}
