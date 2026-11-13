package com.marlonleoner.musicando.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum RoleEnum {

    HOST,
    PLAYER;

    public static RoleEnum from(String value) {
        return Arrays.stream(values())
                .filter(role -> role.toString().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Role inválido: " + value));
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
