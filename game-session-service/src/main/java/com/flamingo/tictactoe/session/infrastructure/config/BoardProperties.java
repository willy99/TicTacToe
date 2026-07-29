package com.flamingo.tictactoe.session.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * The board size the session service plays automated games on. Reading it
 * from config instead of hardcoding it means it can be changed per
 * environment, and SessionService doesn't need to hardcode "3" while the
 * Game Engine itself can handle any board size.
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
