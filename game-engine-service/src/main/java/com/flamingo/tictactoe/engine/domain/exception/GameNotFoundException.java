package com.flamingo.tictactoe.engine.domain.exception;

/**
 * Raised when a game lookup is attempted for an id that does not exist.
 */
public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException(String gameId) {
        super("Game '%s' was not found".formatted(gameId));
    }
}
