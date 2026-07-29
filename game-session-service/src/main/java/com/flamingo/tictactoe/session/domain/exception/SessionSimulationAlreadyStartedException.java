package com.flamingo.tictactoe.session.domain.exception;

/**
 * Raised when {@code POST /sessions/{id}/simulate} is called for a session
 * whose automated simulation is already under way (as opposed to
 * {@link SessionAlreadyCompletedException}, which covers a session that has
 * already finished).
 */
public class SessionSimulationAlreadyStartedException extends RuntimeException {

    public SessionSimulationAlreadyStartedException(String sessionId) {
        super("Session '%s' is already being simulated".formatted(sessionId));
    }
}
