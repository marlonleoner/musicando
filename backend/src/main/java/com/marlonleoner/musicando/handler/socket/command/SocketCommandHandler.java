package com.marlonleoner.musicando.handler.socket.command;

import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import tools.jackson.databind.JsonNode;

public interface SocketCommandHandler {

    void handle(SocketPrincipal principal, JsonNode payload) throws BaseException;
}
