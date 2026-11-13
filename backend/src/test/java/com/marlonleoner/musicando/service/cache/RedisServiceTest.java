package com.marlonleoner.musicando.service.cache;

import com.marlonleoner.musicando.domain.enums.RoleEnum;
import com.marlonleoner.musicando.domain.socket.SocketAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    private static final String KEY = "@MUSICANDO:SESSIONS:session-1";

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private RedisService service;

    @BeforeEach
    void setUp() {
        service = new RedisService(redisTemplate, JsonMapper.builder().build());
    }

    @Test
    void shouldStoreValueWithExpirationWhenSetting() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service.set(KEY, Boolean.TRUE, 3600);

        verify(valueOperations).set(KEY, Boolean.TRUE, 3600, TimeUnit.SECONDS);
    }

    @Test
    void shouldReturnEmptyWhenKeyDoesNotExist() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(KEY)).thenReturn(null);

        assertThat(service.get(KEY, SocketAttributes.class)).isEmpty();
    }

    @Test
    void shouldConvertStoredValueWhenKeyExists() {
        final Map<String, Object> stored = Map.of("sessionId", "session-1", "id", "room-id", "code", "ABC123",
                "secret", "room-secret", "role", "host");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(KEY)).thenReturn(stored);

        assertThat(service.get(KEY, SocketAttributes.class))
                .contains(new SocketAttributes("session-1", "room-id", "ABC123", "room-secret", RoleEnum.HOST));
    }

    @Test
    void shouldDeleteKeyWhenInvalidating() {
        service.invalidate(KEY);

        verify(redisTemplate).delete(KEY);
    }
}
