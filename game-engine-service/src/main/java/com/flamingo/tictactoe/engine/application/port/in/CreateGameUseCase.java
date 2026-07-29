package com.flamingo.tictactoe.engine.application.port.in;

import com.flamingo.tictactoe.engine.domain.model.GameSnapshot;

/**
 * Creates a new game for the given id, or just returns the existing one if
 * it's already there (safe to call twice). boardSize only matters the
 * first time a given gameId is created - after that, the game keeps
 * whatever size it started with.
 */
public interface CreateGameUseCase {

    GameSnapshot initializeGame(String gameId, int boardSize);
}
