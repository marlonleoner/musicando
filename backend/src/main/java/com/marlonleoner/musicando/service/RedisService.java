package com.marlonleoner.musicando.service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.marlonleoner.musicando.mapper.JSONMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object object, Integer expireIn) {
        redisTemplate.opsForValue().set(key, object, expireIn, TimeUnit.SECONDS);
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        Object object = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(JSONMapper.deserialize(object, type));
    }

    public void invalidate(String key) {
        redisTemplate.delete(key);
    }

}
