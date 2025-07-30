package com.marlonleoner.musicando.util;

import com.github.f4b6a3.uuid.UuidCreator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UUID7 {

    public static String generate() {
        return UuidCreator.getTimeOrderedEpoch().toString();
    }
}
