package com.flamingo.tictactoe.session.application.port.in;

import com.flamingo.tictactoe.session.domain.model.SessionSnapshot;

/**
 * Inbound port: automatically play out a session to completion, alternating
 * moves between the two automated players until the game ends in a win or a draw.
 */
public interface SimulateGameUseCase {

    SessionSnapshot simulate(String sessionId);
}
