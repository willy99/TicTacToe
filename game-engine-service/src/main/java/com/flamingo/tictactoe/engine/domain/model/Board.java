package com.flamingo.tictactoe.engine.domain.model;

import com.flamingo.tictactoe.engine.domain.exception.CellOccupiedException;

/**
 * Mutable 3x3 grid of {@link Symbol}s. Owned exclusively by {@link Game}; nothing
 * outside the aggregate should be able to obtain a reference to a mutable Board.
 */
final class Board {

    private final Symbol[][] cells = new Symbol[Position.BOARD_SIZE][Position.BOARD_SIZE];

    void place(Position position, Symbol symbol) {
        if (cells[position.row()][position.col()] != null) {
            throw new CellOccupiedException(position);
        }
        cells[position.row()][position.col()] = symbol;
    }

    boolean isFull() {
        for (Symbol[] row : cells) {
            for (Symbol cell : row) {
                if (cell == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns whether {@code symbol} currently occupies a full row, column, or diagonal.
     */
    boolean hasWinningLineFor(Symbol symbol) {
        for (int i = 0; i < Position.BOARD_SIZE; i++) {
            if (lineMatches(symbol, cells[i][0], cells[i][1], cells[i][2])) {
                return true;
            }
            if (lineMatches(symbol, cells[0][i], cells[1][i], cells[2][i])) {
                return true;
            }
        }
        return lineMatches(symbol, cells[0][0], cells[1][1], cells[2][2])
                || lineMatches(symbol, cells[0][2], cells[1][1], cells[2][0]);
    }

    private boolean lineMatches(Symbol symbol, Symbol a, Symbol b, Symbol c) {
        return symbol == a && symbol == b && symbol == c;
    }

    /**
     * Defensive copy of the current cell contents, safe to hand out to callers.
     */
    Symbol[][] snapshotCells() {
        Symbol[][] copy = new Symbol[Position.BOARD_SIZE][Position.BOARD_SIZE];
        for (int i = 0; i < Position.BOARD_SIZE; i++) {
            copy[i] = cells[i].clone();
        }
        return copy;
    }
}
