package com.marlonleoner.musicando.service.socket;

import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.SocketCommandException;
import com.marlonleoner.musicando.domain.socket.SocketCommandType;
import com.marlonleoner.musicando.domain.socket.SocketErrorCode;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.dto.socket.SocketCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * Encaminha cada comando ao handler do seu tipo, verificando antes se o papel do remetente pode enviá-lo.
 */
@Service
@RequiredArgsConstructor
public class SocketCommandDispatcher {

    private final ApplicationContext context;

    public void dispatch(SocketPrincipal principal, SocketCommand command) throws BaseException {
        final SocketCommandType type = SocketCommandType.from(command.type())
                .orElseThrow(() -> new SocketCommandException(SocketErrorCode.INVALID_COMMAND, "Unknown command type"));

        if (!type.isAllowedFor(principal.role())) {
            throw new SocketCommandException(SocketErrorCode.FORBIDDEN, "Command not allowed for this role");
        }

        context.getBean(type.getHandler()).handle(principal, command.payload());
    }
}
