package com.flamingo.tictactoe.session.domain.exception;

/**
 * Raised when the Game Session Service cannot successfully complete a call to
 * the Game Engine Service, whether because of a network failure or because the
 * Game Engine returned an unexpected error response.
 */
public class GameEngineCommunicationException extends RuntimeException {

    public GameEngineCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
