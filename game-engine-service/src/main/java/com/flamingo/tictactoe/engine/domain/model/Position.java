package com.flamingo.tictactoe.engine.domain.model;

import com.flamingo.tictactoe.engine.domain.exception.InvalidMoveException;

/**
 * A zero-based (row, column) coordinate.
 *
 * <p>Only non-negativity is a universal invariant of a coordinate, so that's
 * all this record enforces on construction. Whether a given (row, col) is
 * actually within a particular game's board is board-size-dependent - that
 * check belongs to {@link Board}, the only class that knows its own size,
 * not to Position (which would otherwise have to hardcode a fixed board
 * dimension and couldn't support configurable board sizes, e.g. 6x6).
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
