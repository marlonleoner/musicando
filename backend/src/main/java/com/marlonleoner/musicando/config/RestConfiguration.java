package com.marlonleoner.musicando.config;

import com.marlonleoner.musicando.config.properties.CorsProperties;
import com.marlonleoner.musicando.handler.RateLimitInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RestConfiguration {

    @Bean
    public WebMvcConfigurer corsConfigurer(final CorsProperties corsProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(corsProperties.allowedOriginsAsArray())
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD");
            }
        };
    }

    @Bean
    public WebMvcConfigurer rateLimitConfigurer(final RateLimitInterceptor rateLimitInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(rateLimitInterceptor)
                        .addPathPatterns(RateLimitInterceptor.CREATE_ROOM_PATH, RateLimitInterceptor.JOIN_ROOM_PATH);
            }
        };
    }
}
