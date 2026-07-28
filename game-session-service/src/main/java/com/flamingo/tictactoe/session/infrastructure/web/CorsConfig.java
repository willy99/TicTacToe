package com.flamingo.tictactoe.session.infrastructure.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Allows the React UI (served from a different origin during development,
 * e.g. the Vite dev server) to call this API directly. The allowed origin is
 * externalized so it can be tightened per environment.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${ui.allowed-origin:http://localhost:5173}")
    private String allowedOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/sessions/**")
                .allowedOrigins(allowedOrigin)
                .allowedMethods("GET", "POST")
                .allowedHeaders("*");
    }
}
