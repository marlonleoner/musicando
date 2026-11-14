package com.marlonleoner.musicando.validator;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.RoomStatus;
import com.marlonleoner.musicando.domain.exception.InvalidNicknameException;
import com.marlonleoner.musicando.domain.exception.NicknameTakenException;
import com.marlonleoner.musicando.domain.exception.RoomFullException;
import com.marlonleoner.musicando.domain.exception.RoomNotJoinableException;
import com.marlonleoner.musicando.fixture.Fixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JoinRoomValidatorTest {

    private static final int MAX_PLAYERS = 2;

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "ana|ANA",
        "  Ana Maria  |ANA MARIA",
        "joão   pedro|JOÃO PEDRO",
        "dj_marlon-2.0|DJ_MARLON-2.0",
        "ab|AB",
        "abcdefghijklmnop|ABCDEFGHIJKLMNOP",
    })
    void shouldNormalizeNicknameWhenNicknameIsValid(final String nickname, final String expected) throws InvalidNicknameException {
        assertThat(JoinRoomValidator.normalizeNickname(nickname)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "a", "  a  ", "abcdefghijklmnopq", "ana<script>", "ana@", "emoji 🎵"})
    void shouldThrowInvalidNicknameExceptionWhenNicknameIsInvalid(final String nickname) {
        assertThatThrownBy(() -> JoinRoomValidator.normalizeNickname(nickname)).isInstanceOf(InvalidNicknameException.class);
    }

    @Test
    void shouldAcceptPlayerWhenRoomIsWaitingAndHasRoom() {
        final Room room = room(RoomStatus.WAITING, 1);

        assertThatCode(() -> JoinRoomValidator.validateRoom(room, "BRUNO", MAX_PLAYERS)).doesNotThrowAnyException();
    }

    @Test
    void shouldAcceptPlayerWhenRoomHasNoPlayersList() {
        final Room room = room(RoomStatus.WAITING, 0);
        room.setPlayers(null);

        assertThatCode(() -> JoinRoomValidator.validateRoom(room, "BRUNO", MAX_PLAYERS)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(value = RoomStatus.class, names = "WAITING", mode = EnumSource.Mode.EXCLUDE)
    void shouldThrowRoomNotJoinableExceptionWhenRoomIsNotWaiting(final RoomStatus status) {
        final Room room = room(status, 0);

        assertThatThrownBy(() -> JoinRoomValidator.validateRoom(room, "BRUNO", MAX_PLAYERS)).isInstanceOf(RoomNotJoinableException.class);
    }

    @Test
    void shouldThrowRoomFullExceptionWhenRoomReachedMaximumOfPlayers() {
        final Room room = room(RoomStatus.WAITING, MAX_PLAYERS);

        assertThatThrownBy(() -> JoinRoomValidator.validateRoom(room, "BRUNO", MAX_PLAYERS)).isInstanceOf(RoomFullException.class);
    }

    @Test
    void shouldThrowNicknameTakenExceptionWhenNicknameIsAlreadyInUse() {
        final Room room = room(RoomStatus.WAITING, 1);
        room.getPlayers().getFirst().setNickname("BRUNO");

        assertThatThrownBy(() -> JoinRoomValidator.validateRoom(room, "BRUNO", MAX_PLAYERS)).isInstanceOf(NicknameTakenException.class);
    }

    private Room room(final RoomStatus status, final int players) {
        final Room room = Fixture.make(Room.class);
        room.setStatus(status);
        room.setPlayers(new ArrayList<>(Fixture.makeList(Player.class, players)));

        return room;
    }
}
