package com.flamingo.tictactoe.session.application.port.in;

import com.flamingo.tictactoe.session.domain.model.SessionSnapshot;

/**
 * Inbound port: retrieve the current state of a session, including its move history.
 */
public interface GetSessionUseCase {

    SessionSnapshot getSession(String sessionId);
}
