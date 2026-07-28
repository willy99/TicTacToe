package com.flamingo.tictactoe.engine.domain.exception;

import com.flamingo.tictactoe.engine.domain.model.Position;

/**
 * Raised when a move targets a cell that is already occupied.
 */
public class CellOccupiedException extends InvalidMoveException {

    public CellOccupiedException(Position position) {
        super("Cell (%d, %d) is already occupied".formatted(position.row(), position.col()));
    }
}
