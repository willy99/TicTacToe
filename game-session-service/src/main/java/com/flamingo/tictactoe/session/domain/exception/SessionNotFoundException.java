package com.flamingo.tictactoe.session.domain.exception;

/**
 * Raised when a session lookup is attempted for an id that does not exist.
 */
public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException(String sessionId) {
        super("Session '%s' was not found".formatted(sessionId));
    }
}
