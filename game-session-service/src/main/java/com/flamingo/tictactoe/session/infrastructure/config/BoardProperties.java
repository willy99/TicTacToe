package com.flamingo.tictactoe.session.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externalizes the board size the session service plays automated games on.
 * A bean (rather than a constant) so it can be tuned per environment without
 * a code change, and so {@code SessionService} doesn't hardcode "3" while the
 * Game Engine itself supports arbitrary board sizes.
 */
@Component
@ConfigurationProperties(prefix = "game.board")
public class BoardProperties {

    private int size = 3;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
