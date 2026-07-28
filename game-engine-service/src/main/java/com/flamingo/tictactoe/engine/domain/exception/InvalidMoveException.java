package com.flamingo.tictactoe.engine.domain.exception;

/**
 * Raised when a requested move breaks the rules of the game
 * (out-of-bounds position, malformed input, etc.).
 */
public class InvalidMoveException extends RuntimeException {

    public InvalidMoveException(String message) {
        super(message);
    }
}
