package com.flamingo.tictactoe.engine.domain.model;

import com.flamingo.tictactoe.engine.domain.exception.CellOccupiedException;
import com.flamingo.tictactoe.engine.domain.exception.InvalidMoveException;

/**
 * Mutable {@code size x size} grid of {@link Symbol}s. Owned exclusively by
 * {@link Game}; nothing outside the aggregate should be able to obtain a
 * reference to a mutable Board.
 *
 * <p>The board's dimension is a constructor parameter rather than a fixed
 * constant, so the same win-detection logic works for classic 3x3 as well as
 * larger boards (e.g. 6x6, where winning means filling an entire row, column,
 * or diagonal of 6). {@link Position} only guarantees non-negative
 * coordinates; whether a coordinate actually fits *this* board is this
 * class's responsibility, since only the board knows its own size.
 */
final class Board {

    static final int DEFAULT_SIZE = 3;
    private static final int MIN_SIZE = 3;

    private final int size;
    private final Symbol[][] cells;
    private int filledCellCount;

    Board() {
        this(DEFAULT_SIZE);
    }

    Board(int size) {
        if (size < MIN_SIZE) {
            throw new IllegalArgumentException("Board size must be at least " + MIN_SIZE);
        }
        this.size = size;
        this.cells = new Symbol[size][size];
    }

    int size() {
        return size;
    }

    void place(Position position, Symbol symbol) {
        requireWithinBounds(position);
        if (cells[position.row()][position.col()] != null) {
            throw new CellOccupiedException(position);
        }
        cells[position.row()][position.col()] = symbol;
        filledCellCount++;
    }

    private void requireWithinBounds(Position position) {
        if (position.row() >= size || position.col() >= size) {
            throw new InvalidMoveException(
                    "Position (%d, %d) is outside the %dx%d board".formatted(position.row(), position.col(), size, size));
        }
    }

    /**
     * O(1): {@link #place} is the board's only mutation point, so it keeps
     * {@link #filledCellCount} in sync instead of rescanning every cell here.
     */
    boolean isFull() {
        return filledCellCount == size * size;
    }

    /**
     * Returns whether {@code symbol} currently occupies a full row, column, or
     * diagonal - generalized to the board's actual size, so a 6x6 board needs
     * 6 in a line, not a hardcoded 3.
     */
    boolean hasWinningLineFor(Symbol symbol) {
        for (int i = 0; i < size; i++) {
            if (rowMatches(symbol, i) || columnMatches(symbol, i)) {
                return true;
            }
        }
        return diagonalMatches(symbol) || antiDiagonalMatches(symbol);
    }

    private boolean rowMatches(Symbol symbol, int row) {
        for (int col = 0; col < size; col++) {
            if (cells[row][col] != symbol) {
                return false;
            }
        }
        return true;
    }

    private boolean columnMatches(Symbol symbol, int col) {
        for (int row = 0; row < size; row++) {
            if (cells[row][col] != symbol) {
                return false;
            }
        }
        return true;
    }

    private boolean diagonalMatches(Symbol symbol) {
        for (int i = 0; i < size; i++) {
            if (cells[i][i] != symbol) {
                return false;
            }
        }
        return true;
    }

    private boolean antiDiagonalMatches(Symbol symbol) {
        for (int i = 0; i < size; i++) {
            if (cells[i][size - 1 - i] != symbol) {
                return false;
            }
        }
        return true;
    }

    /**
     * Defensive copy of the current cell contents, safe to hand out to callers.
     */
    Symbol[][] snapshotCells() {
        Symbol[][] copy = new Symbol[size][size];
        for (int i = 0; i < size; i++) {
            copy[i] = cells[i].clone();
        }
        return copy;
    }
}
