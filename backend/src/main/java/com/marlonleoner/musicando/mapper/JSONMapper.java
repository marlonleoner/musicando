package com.marlonleoner.musicando.mapper;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JSONMapper {

    private static final Gson GSON = new Gson();

    public static String serialize(Object object) {
        return GSON.toJson(object);
    }

    public static <T> T deserialize(Object object, Class<T> type) {
        if (object instanceof String value) {
            return GSON.fromJson(value, type);
        }

        return GSON.fromJson(GSON.toJson(object), type);
    }
}
