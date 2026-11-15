package com.marlonleoner.musicando.service.socket;

import com.marlonleoner.musicando.config.properties.RateLimitProperties;
import com.marlonleoner.musicando.domain.enums.ErrorCode;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.SocketCommandException;
import com.marlonleoner.musicando.domain.socket.SocketCommandType;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.dto.socket.SocketCommand;
import com.marlonleoner.musicando.service.ratelimit.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Encaminha cada comando ao handler do seu tipo, verificando antes se o papel do remetente pode enviá-lo.
 */
@Service
@RequiredArgsConstructor
public class SocketCommandDispatcher {

    private static final Duration ONE_SECOND = Duration.ofSeconds(1);

    private final ApplicationContext context;

    private final RateLimiter rateLimiter;

    private final RateLimitProperties rateLimitProperties;

    public void dispatch(SocketPrincipal principal, SocketCommand command) throws BaseException {
        if (!rateLimiter.tryAcquire("command:" + principal.participantId(), rateLimitProperties.commandsPerSecond(), ONE_SECOND)) {
            throw new SocketCommandException(ErrorCode.RATE_LIMITED, "Too many commands, slow down");
        }

        final SocketCommandType type = SocketCommandType.from(command.type())
                .orElseThrow(() -> new SocketCommandException(ErrorCode.INVALID_COMMAND, "Unknown command type"));

        if (!type.isAllowedFor(principal.role())) {
            throw new SocketCommandException(ErrorCode.FORBIDDEN, "Command not allowed for this role");
        }

        context.getBean(type.getHandler()).handle(principal, command.payload());
    }
}
