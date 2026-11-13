package com.marlonleoner.musicando.fixture;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;

public class RandomUtil {

    public static <T> T pick(final T... values) {
        if (isEmpty(values)) {
            return null;
        }

        return values[ThreadLocalRandom.current().nextInt(0, values.length)];
    }

    public static <T> T pickExcept(final T[] values, final T... exceptions) {
        return (T) pick(
                Stream.of(values)
                        .filter(v -> Stream.of(exceptions).noneMatch(v::equals))
                        .toArray()
        );
    }
}
