package com.flamingo.tictactoe.engine.domain.model;

import com.flamingo.tictactoe.engine.domain.exception.InvalidMoveException;

/**
 * A zero-based (row, column) coordinate.
 *
 * <p>Only checks here that row/col aren't negative. Whether a position is
 * actually on a particular game's board depends on how big that board is,
 * and only Board knows that, so that check happens there instead.
 */
public record Position(int row, int col) {

    public Position {
        // validate first
        if (row < 0 || col < 0) {
            throw new InvalidMoveException(
                    "Position (%d, %d) has a negative coordinate".formatted(row, col));
        }
    }
}
