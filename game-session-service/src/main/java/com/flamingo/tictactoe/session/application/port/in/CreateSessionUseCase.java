package com.flamingo.tictactoe.session.application.port.in;

import com.flamingo.tictactoe.session.domain.model.SessionSnapshot;

/**
 * Creates a new game session and sets up its game in the Game Engine.
 */
public interface CreateSessionUseCase {

    SessionSnapshot createSession();
}
