package com.marlonleoner.musicando.handler.socket;

import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.socket.SocketAttributes;

public abstract class AbstractSocketHandler {

    protected abstract void handle(SocketAttributes attributes) throws BaseException;
}
