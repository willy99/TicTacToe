package com.flamingo.tictactoe.engine.domain.model;

import com.flamingo.tictactoe.engine.domain.exception.CellOccupiedException;
import com.flamingo.tictactoe.engine.domain.exception.InvalidMoveException;

/**
 * A mutable size x size grid of symbols. Only Game is allowed to touch a
 * Board directly - nothing outside it should hold a reference to one.
 *
 * <p>The size is passed into the constructor instead of being a fixed
 * number, so the same win-checking code works for a regular 3x3 board and
 * for bigger ones too (e.g. 6x6, where you need 6 in a row to win).
 * Position only makes sure row/col aren't negative; checking whether a
 * position actually fits on this particular board is Board's job, since
 * only Board knows how big it is.
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
     * place() is the only place that fills a cell, so it just keeps a
     * running count here instead of scanning every cell each time.
     */
    boolean isFull() {
        return filledCellCount == size * size;
    }

    /**
     * True if symbol fills a whole row, column, or diagonal. Works for any
     * board size, so a 6x6 board needs 6 in a line, not always 3.
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
     * Makes a copy of the current cells, safe to hand out to callers.
     */
    Symbol[][] snapshotCells() {
        Symbol[][] copy = new Symbol[size][size];
        for (int i = 0; i < size; i++) {
            copy[i] = cells[i].clone();
        }
        return copy;
    }
}
