package com.marlonleoner.musicando.domain.socket;

import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import com.marlonleoner.musicando.domain.enums.RoleEnum;

public record SocketAttributes(
        String sessionId,
        String id,
        String code,
        String secret,
        RoleEnum role) {

    public static SocketAttributes fromHeaders(StompHeaderAccessor accessor) {
        return new SocketAttributes(
                accessor.getSessionId(),
                accessor.getFirstNativeHeader("id"),
                accessor.getFirstNativeHeader("code"),
                accessor.getFirstNativeHeader("secret"),
                RoleEnum.from(accessor.getFirstNativeHeader("role")));
    }
}
