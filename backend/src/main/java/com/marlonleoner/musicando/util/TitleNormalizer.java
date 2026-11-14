package com.marlonleoner.musicando.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Normaliza títulos de músicas para exibição e comparação: remove indicações de versão no fim do título (trechos entre
 * parênteses ou colchetes e sufixos após " - "), reduz espaços e converte para caixa alta.
 *
 * <p>Exemplos: "Song (Remastered 2011)", "Song - Ao Vivo" e "Song [feat. X]" viram "SONG". Parênteses no início do
 * título são mantidos: "(I Can't Get No) Satisfaction" vira "(I CAN'T GET NO) SATISFACTION".</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TitleNormalizer {

    private static final Pattern TRAILING_GROUP = Pattern.compile("\\s*[(\\[][^()\\[\\]]*[)\\]]\\s*$");

    private static final Pattern DASH_SUFFIX = Pattern.compile("\\s+-\\s+.*$");

    private static final Pattern WHITESPACES = Pattern.compile("\\s+");

    public static String normalize(final String title) {
        if (Objects.isNull(title)) {
            return "";
        }

        final String collapsed = WHITESPACES.matcher(title.strip()).replaceAll(" ");
        String result = DASH_SUFFIX.matcher(collapsed).replaceFirst("");
        String previous;
        do {
            previous = result;
            result = TRAILING_GROUP.matcher(result).replaceFirst("");
        } while (!result.equals(previous) && !result.isBlank());

        if (result.isBlank()) {
            result = collapsed;
        }

        return result.strip().toUpperCase(Locale.ROOT);
    }
}
