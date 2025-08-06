package com.marlonleoner.musicando.mapper;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JSONMapper {

    private static final Gson gson = new Gson();

    public static String serialize(Object object) {
        return gson.toJson(object);
    }

    public static <T> T deserialize(Object object, Class<T> type) {
        if (object instanceof String value)
            return gson.fromJson(value, type);

        return gson.fromJson(gson.toJson(object), type);
    }
}
