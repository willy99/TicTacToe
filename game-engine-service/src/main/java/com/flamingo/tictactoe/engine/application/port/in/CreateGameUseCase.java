package com.flamingo.tictactoe.engine.application.port.in;

import com.flamingo.tictactoe.engine.domain.model.GameSnapshot;

/**
 * Inbound port: initialize a new game for a given id, or return the existing
 * one unchanged if it was already created (idempotent).
 */
public interface CreateGameUseCase {

    GameSnapshot initializeGame(String gameId);
}
