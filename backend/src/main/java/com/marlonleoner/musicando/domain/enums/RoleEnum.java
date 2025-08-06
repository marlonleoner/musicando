package com.marlonleoner.musicando.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RoleEnum {

    HOST,
    PLAYER;

    public static RoleEnum from(String value) {
        try {
            return RoleEnum.valueOf(value.toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Role inválido: " + value);
        }
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}