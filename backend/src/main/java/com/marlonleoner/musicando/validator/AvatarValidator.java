package com.marlonleoner.musicando.validator;

import com.marlonleoner.musicando.domain.exception.InvalidAvatarException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

/**
 * Personagens (avatares) que os jogadores podem escolher. Cada um tem as imagens {@code <id>-idle.webp},
 * {@code <id>-cheer.webp} e {@code <id>-face.webp} em {@code public/avatars} dos apps, geradas por
 * {@code tools/avatars}. A mesma lista existe no app do celular ({@code avatars.ts}) e no script de geração.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AvatarValidator {

    public static final List<String> AVATARS = List.of(
            "alien", "female-a", "female-b", "female-c", "female-d", "female-e", "female-f", "knight", "male-a", "male-b",
            "male-c", "male-d", "male-e", "male-f", "male-g", "raider", "robot", "soldier", "worker", "zombie");

    public static void validate(final String avatar) throws InvalidAvatarException {
        if (Objects.isNull(avatar) || !AVATARS.contains(avatar)) {
            throw new InvalidAvatarException(avatar);
        }
    }
}
