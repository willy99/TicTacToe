package com.flamingo.tictactoe.session.domain.exception;

import com.flamingo.tictactoe.session.domain.model.SessionStatus;

/**
 * Raised when a simulation is requested for, or a move is recorded against,
 * a session that has already concluded (win or draw).
 */
public class SessionAlreadyCompletedException extends RuntimeException {

    public SessionAlreadyCompletedException(String sessionId, SessionStatus status) {
        super("Session '%s' has already completed with status %s".formatted(sessionId, status));
    }
}
