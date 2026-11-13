package com.marlonleoner.musicando.service.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private final JsonMapper jsonMapper;

    public void set(String key, Object object, Integer expireIn) {
        redisTemplate.opsForValue().set(key, object, expireIn, TimeUnit.SECONDS);
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key))
                .map(value -> jsonMapper.convertValue(value, type));
    }

    public void invalidate(String key) {
        redisTemplate.delete(key);
    }
}
