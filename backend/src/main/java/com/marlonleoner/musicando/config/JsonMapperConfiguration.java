package com.marlonleoner.musicando.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;

import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class JsonMapperConfiguration {

    public static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    @Bean
    JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
        return builder -> builder
                .changeDefaultVisibility(visibility -> visibility.withFieldVisibility(Visibility.ANY))
                .changeDefaultPropertyInclusion(inclusion -> inclusion.withValueInclusion(Include.NON_NULL))
                .defaultTimeZone(TimeZone.getTimeZone(SAO_PAULO))
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    }
}
