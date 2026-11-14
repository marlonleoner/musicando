package com.marlonleoner.musicando.validator;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.exception.InvalidNicknameException;
import com.marlonleoner.musicando.domain.exception.NicknameTakenException;
import com.marlonleoner.musicando.domain.exception.RoomFullException;
import com.marlonleoner.musicando.domain.exception.RoomNotJoinableException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Regras de entrada de um jogador em uma sala.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JoinRoomValidator {

    public static final int NICKNAME_MIN_LENGTH = 2;

    public static final int NICKNAME_MAX_LENGTH = 16;

    private static final Pattern WHITESPACES = Pattern.compile("\\s+");

    private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("^[\\p{L}\\p{N} ._-]+$");

    /**
     * Normaliza o apelido: remove espaços nas pontas, reduz espaços repetidos a um só e converte para caixa alta.
     *
     * @return apelido normalizado
     * @throws InvalidNicknameException se o apelido for vazio, tiver tamanho fora de 2 a 16 caracteres ou conter
     *                                  caracteres diferentes de letras, números, espaço, ponto, hífen e sublinhado
     */
    public static String normalizeNickname(String nickname) throws InvalidNicknameException {
        if (Objects.isNull(nickname)) {
            throw new InvalidNicknameException("Nickname is required");
        }

        final String normalized = WHITESPACES.matcher(nickname.strip()).replaceAll(" ").toUpperCase(Locale.ROOT);

        if (normalized.length() < NICKNAME_MIN_LENGTH || normalized.length() > NICKNAME_MAX_LENGTH) {
            throw new InvalidNicknameException(String.format("Nickname must have between %d and %d characters", NICKNAME_MIN_LENGTH, NICKNAME_MAX_LENGTH));
        }

        if (!ALLOWED_CHARACTERS.matcher(normalized).matches()) {
            throw new InvalidNicknameException("Nickname may contain only letters, numbers, spaces, dots, hyphens and underscores");
        }

        return normalized;
    }

    /**
     * Valida se a sala aceita um novo jogador com o apelido informado (já normalizado).
     *
     * @throws RoomNotJoinableException se a sala não estiver aguardando jogadores
     * @throws RoomFullException        se a sala já tiver o máximo de jogadores
     * @throws NicknameTakenException   se o apelido já estiver em uso na sala
     */
    public static void validateRoom(Room room, String nickname, int maxPlayers)
            throws RoomNotJoinableException, RoomFullException, NicknameTakenException {
        if (!room.isWaiting()) {
            throw new RoomNotJoinableException(room.getCode(), room.getStatus());
        }

        final List<Player> players = Objects.requireNonNullElse(room.getPlayers(), List.of());

        if (players.size() >= maxPlayers) {
            throw new RoomFullException(room.getCode(), maxPlayers);
        }

        if (players.stream().anyMatch(player -> nickname.equals(player.getNickname()))) {
            throw new NicknameTakenException(nickname);
        }
    }
}
