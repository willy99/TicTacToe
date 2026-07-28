package com.flamingo.tictactoe.engine.domain.model;

import com.flamingo.tictactoe.engine.domain.exception.InvalidMoveException;

/**
 * A zero-based (row, column) coordinate on the 3x3 board.
 * Validates its own bounds so illegal coordinates can never exist as an object.
 */
public record Position(int row, int col) {

    public static final int BOARD_SIZE = 3;

    public Position {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            throw new InvalidMoveException(
                    "Position (%d, %d) is outside the 3x3 board".formatted(row, col));
        }
    }
}
