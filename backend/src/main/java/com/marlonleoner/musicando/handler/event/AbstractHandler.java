package com.marlonleoner.musicando.handler.event;

import com.marlonleoner.musicando.domain.event.Event;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.mapper.JSONMapper;
import com.marlonleoner.musicando.util.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractHandler {

    protected final JSONMapper converter;

    protected abstract void handle(Event payload) throws BaseException;

    // Ponto de entrada dos eventos de jogo: nenhuma falha de handler pode interromper o consumo da fila
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void onHandle(Event payload) {
        try {
            handle(payload);
        } catch (Exception ex) {
            log.error("An error occurred while processing event in room - reason {}", LogSanitizer.sanitize(ex.getMessage()));
        }
    }
}
