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
 * Limita, por IP, a criação de salas e as tentativas de entrar em uma sala (o que também dificulta adivinhar códigos).
 */
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    public static final String CREATE_ROOM_PATH = "/room";

    public static final String JOIN_ROOM_PATH = "/room/join";

    private static final Duration ONE_MINUTE = Duration.ofMinutes(1);

    private final RateLimiter rateLimiter;

    private final RateLimitProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws TooManyRequestsException {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return true;
        }

        final String path = request.getRequestURI().substring(request.getContextPath().length());
        final String ip = request.getRemoteAddr();

        if (CREATE_ROOM_PATH.equals(path) && !rateLimiter.tryAcquire("room-create:" + ip, properties.roomCreationsPerMinute(), ONE_MINUTE)) {
            throw new TooManyRequestsException();
        }

        if (JOIN_ROOM_PATH.equals(path) && !rateLimiter.tryAcquire("room-join:" + ip, properties.roomJoinsPerMinute(), ONE_MINUTE)) {
            throw new TooManyRequestsException();
        }

        return true;
    }
}
