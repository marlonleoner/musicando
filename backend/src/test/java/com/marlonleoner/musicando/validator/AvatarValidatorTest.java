package com.marlonleoner.musicando.validator;

import com.marlonleoner.musicando.domain.exception.InvalidAvatarException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvatarValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"alien", "female-a", "robot", "zombie"})
    void shouldAcceptAvatarWhenItIsInTheCatalog(final String avatar) {
        assertThatCode(() -> AvatarValidator.validate(avatar)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ROBOT", "unicorn", "male-z", " robot"})
    void shouldRejectAvatarWhenItIsNotInTheCatalog(final String avatar) {
        assertThatThrownBy(() -> AvatarValidator.validate(avatar)).isInstanceOf(InvalidAvatarException.class);
    }
}
