package com.flamingo.tictactoe.engine.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * The board size used for a new game when whoever calls
 * PUT /games/{gameId} doesn't specify one. Reading it from config instead
 * of hardcoding it means it can be changed per environment without
 * touching the code.
 */
@Component
@ConfigurationProperties(prefix = "game-engine.board")
public class BoardProperties {

    private int defaultSize = 3;

    public int getDefaultSize() {
        return defaultSize;
    }

    public void setDefaultSize(int defaultSize) {
        this.defaultSize = defaultSize;
    }
}
