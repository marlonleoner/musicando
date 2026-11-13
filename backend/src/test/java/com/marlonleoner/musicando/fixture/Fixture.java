package com.marlonleoner.musicando.fixture;

import org.instancio.Instancio;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Fixture {


    public static <T> T make(final Class<T> clazz) {
        return Instancio.create(clazz);
    }

    public static <T> List<T> makeList(final Class<T> clazz) {
        return makeList(clazz, 1);
    }

    public static <T> List<T> makeList(final Class<T> clazz, final Integer size) {
        return Instancio.ofList(clazz)
                .size(size)
                .create();
    }

    public static String getCPFValid() {
        final var cpfs = new ArrayList<String>() {{
            add("17587596585");
            add("54921900590");
            add("84617242082");
            add("68205103658");
            add("76541821046");
            add("67086236098");
            add("08855641387");
            add("72273158190");
            add("94621943430");
            add("36561776275");
        }};

        return cpfs.get(ThreadLocalRandom.current().nextInt(0, cpfs.size()));
    }
}
