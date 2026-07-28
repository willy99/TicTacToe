package com.flamingo.tictactoe.engine.domain.exception;

import com.flamingo.tictactoe.engine.domain.model.GameStatus;

/**
 * Raised when a move is attempted on a game that has already concluded (win or draw).
 */
public class GameAlreadyFinishedException extends RuntimeException {

    public GameAlreadyFinishedException(String gameId, GameStatus status) {
        super("Game '%s' has already finished with status %s".formatted(gameId, status));
    }
}
