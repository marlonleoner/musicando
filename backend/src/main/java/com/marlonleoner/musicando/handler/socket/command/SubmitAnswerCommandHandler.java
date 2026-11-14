package com.marlonleoner.musicando.handler.socket.command;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.ErrorCode;
import com.marlonleoner.musicando.domain.exception.AnswerNotAcceptedException;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.SocketCommandException;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.service.game.GameEngine;
import com.marlonleoner.musicando.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.Objects;

/**
 * {@code SUBMIT_ANSWER} (jogador, durante a música): {@code { "round": 1, "option": 2 }}, com a rodada começando em 1 e
 * o índice da opção escolhida. Vale uma resposta por rodada.
 */
@Component
@RequiredArgsConstructor
public class SubmitAnswerCommandHandler implements SocketCommandHandler {

    private static final String ROUND_FIELD = "round";

    private static final String OPTION_FIELD = "option";

    private final RoomService roomService;

    private final GameEngine gameEngine;

    @Override
    public void handle(SocketPrincipal principal, JsonNode payload) throws BaseException {
        if (!isInteger(payload, ROUND_FIELD) || !isInteger(payload, OPTION_FIELD)) {
            throw new SocketCommandException(ErrorCode.INVALID_COMMAND, "Payload must contain the round and the option");
        }

        final Room room = roomService.getOrException(principal.roomCode());

        if (!room.isInProgress()) {
            throw new AnswerNotAcceptedException("The game is not running");
        }

        gameEngine.submitAnswer(principal.roomCode(), principal.participantId(), payload.get(ROUND_FIELD).asInt(),
                payload.get(OPTION_FIELD).asInt());
    }

    private boolean isInteger(JsonNode payload, String field) {
        return Objects.nonNull(payload) && payload.hasNonNull(field) && payload.get(field).canConvertToInt()
                && payload.get(field).isIntegralNumber();
    }
}
