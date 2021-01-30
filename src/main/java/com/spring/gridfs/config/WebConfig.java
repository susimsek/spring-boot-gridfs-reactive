package com.spring.gridfs.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.WebExceptionHandler;
import org.zalando.problem.spring.webflux.advice.ProblemExceptionHandler;
import org.zalando.problem.spring.webflux.advice.ProblemHandling;

import java.util.Arrays;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WebConfig implements WebFluxConfigurer {

   final long MAX_AGE_SECS = 3600;

    @Bean
    CorsWebFilter corsFilter() {

        CorsConfiguration config = new CorsConfiguration();

        config.setMaxAge(MAX_AGE_SECS);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.setAllowedMethods(Arrays.asList("HEAD", "OPTIONS", "GET", "POST", "PUT", "PATCH", "DELETE"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }

    @Bean
    @Order(-2) // The handler must have precedence over WebFluxResponseStatusExceptionHandler and Spring Boot's ErrorWebExceptionHandler
    public WebExceptionHandler problemExceptionHandler(ObjectMapper mapper, ProblemHandling problemHandling) {
        return new ProblemExceptionHandler(mapper, problemHandling);
    }
}
