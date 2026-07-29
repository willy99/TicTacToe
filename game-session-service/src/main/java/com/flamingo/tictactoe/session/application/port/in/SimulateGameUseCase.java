package com.flamingo.tictactoe.session.application.port.in;

import com.flamingo.tictactoe.session.domain.model.SessionSnapshot;

/**
 * Plays out a session automatically, alternating moves between the two
 * players until the game ends in a win or a draw.
 */
public interface SimulateGameUseCase {

    SessionSnapshot simulate(String sessionId);
}
