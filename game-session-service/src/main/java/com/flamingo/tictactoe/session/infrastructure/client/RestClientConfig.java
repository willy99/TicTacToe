package com.flamingo.tictactoe.session.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Sets up the RestClient used to call the Game Engine. The base URL comes
 * from config so it can point at a different host/port per environment
 * without changing any code.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient gameEngineRestClient(@Value("${game-engine.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
