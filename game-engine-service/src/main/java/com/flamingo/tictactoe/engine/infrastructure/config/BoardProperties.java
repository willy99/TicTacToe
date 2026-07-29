package com.flamingo.tictactoe.engine.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externalizes the board size a newly created game defaults to when the
 * caller of {@code PUT /games/{gameId}} doesn't specify one. Keeping this a
 * bean (rather than a constant) lets ops override it per environment without
 * a code change, and lets the {@link com.flamingo.tictactoe.engine.infrastructure.web.GameController}
 * resolve the default without hardcoding it.
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
