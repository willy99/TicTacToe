package com.flamingo.tictactoe.session.application.port.in;

import com.flamingo.tictactoe.session.domain.model.SessionSnapshot;

/**
 * Inbound port: create a new game session and initialize its game state in
 * the Game Engine Service.
 */
public interface CreateSessionUseCase {

    SessionSnapshot createSession();
}
