package com.marlonleoner.musicando;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MusicandoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusicandoApplication.class, args);
    }
}
