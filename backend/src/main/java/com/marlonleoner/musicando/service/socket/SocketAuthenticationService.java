package com.marlonleoner.musicando.service.socket;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.enums.ErrorCode;
import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.exception.SocketAuthenticationException;
import com.marlonleoner.musicando.domain.socket.SocketPrincipal;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import com.marlonleoner.musicando.util.SocketDestinations;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Autenticação das conexões WebSocket e autorização das inscrições e comandos.
 */
@Service
@RequiredArgsConstructor
public class SocketAuthenticationService {

    private static final Set<String> PRIVATE_DESTINATIONS = Set.of(SocketDestinations.USER_EVENTS_QUEUE, SocketDestinations.USER_ERRORS_QUEUE,
            SocketDestinations.ROOM_SNAPSHOT);

    private final RoomService roomService;

    private final PlayerService playerService;

    /**
     * Valida as credenciais enviadas no {@code CONNECT}.
     *
     * @return identidade da conexão
     * @throws SocketAuthenticationException com {@code INVALID_CREDENTIALS} se qualquer dado for inválido
     */
    public SocketPrincipal authenticate(String role, String code, String id, String secret) {
        if (Objects.isNull(role) || Objects.isNull(code) || Objects.isNull(id) || Objects.isNull(secret)) {
            throw new SocketAuthenticationException(ErrorCode.INVALID_CREDENTIALS);
        }

        final RoleEnum roleEnum = parseRole(role);
        final boolean valid = RoleEnum.HOST.equals(roleEnum) ? isValidHost(code, id, secret) : isValidPlayer(code, id, secret);

        if (!valid) {
            throw new SocketAuthenticationException(ErrorCode.INVALID_CREDENTIALS);
        }

        return new SocketPrincipal(roleEnum, id, code);
    }

    /**
     * Permite inscrições apenas no tópico da própria sala, no tópico exclusivo do host (somente host), nas filas
     * privadas do participante e na foto inicial da sala.
     *
     * @throws SocketAuthenticationException com {@code FORBIDDEN} para qualquer outro destino
     */
    public void authorizeSubscription(SocketPrincipal principal, String destination) {
        final boolean allowed = Objects.nonNull(destination) && PRIVATE_DESTINATIONS.contains(destination)
                || SocketDestinations.roomTopic(principal.roomCode()).equals(destination)
                || principal.isHost() && SocketDestinations.roomHostTopic(principal.roomCode()).equals(destination);

        if (!allowed) {
            throw new SocketAuthenticationException(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * Permite envios apenas para o endpoint de comandos.
     *
     * @throws SocketAuthenticationException com {@code FORBIDDEN} para qualquer outro destino
     */
    public void authorizeSend(String destination) {
        if (!SocketDestinations.COMMAND.equals(destination)) {
            throw new SocketAuthenticationException(ErrorCode.FORBIDDEN);
        }
    }

    private RoleEnum parseRole(String role) {
        try {
            return RoleEnum.from(role);
        } catch (IllegalArgumentException ex) {
            throw new SocketAuthenticationException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    private boolean isValidHost(String code, String roomId, String secret) {
        return roomService.findByCode(code)
                .filter(room -> room.getId().equals(roomId))
                .filter(room -> secretMatches(room.getSecret(), secret))
                .isPresent();
    }

    private boolean isValidPlayer(String code, String playerId, String secret) {
        final Optional<Player> player = playerService.findById(playerId);

        return player.filter(found -> Objects.nonNull(found.getRoom()) && code.equals(found.getRoom().getCode()))
                .filter(found -> secretMatches(found.getSecret(), secret))
                .isPresent();
    }

    private boolean secretMatches(String expected, String provided) {
        return Objects.nonNull(expected)
                && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), provided.getBytes(StandardCharsets.UTF_8));
    }
}
