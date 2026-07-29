package com.flamingo.tictactoe.engine.application.port.in;

import com.flamingo.tictactoe.engine.domain.model.GameSnapshot;

/**
 * Reads the current state of a game.
 */
public interface GetGameUseCase {

    GameSnapshot getGame(String gameId);
}
