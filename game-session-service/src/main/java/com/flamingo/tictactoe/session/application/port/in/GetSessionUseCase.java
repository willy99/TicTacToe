package com.flamingo.tictactoe.session.application.port.in;

import com.flamingo.tictactoe.session.domain.model.SessionSnapshot;

/**
 * Reads a session's current state, including its move history.
 */
public interface GetSessionUseCase {

    SessionSnapshot getSession(String sessionId);
}
