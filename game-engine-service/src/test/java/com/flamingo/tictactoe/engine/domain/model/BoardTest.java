package com.flamingo.tictactoe.engine.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flamingo.tictactoe.engine.domain.exception.CellOccupiedException;
import org.junit.jupiter.api.Test;

class BoardTest {

    @Test
    void placesASymbolOnAnEmptyCell() {
        Board board = new Board();

        board.place(new Position(0, 0), Symbol.X);

        assertThat(board.snapshotCells()[0][0]).isEqualTo(Symbol.X);
    }

    @Test
    void rejectsAMoveOnAnOccupiedCell() {
        Board board = new Board();
        board.place(new Position(1, 1), Symbol.X);

        assertThatThrownBy(() -> board.place(new Position(1, 1), Symbol.O))
                .isInstanceOf(CellOccupiedException.class);
    }

    @Test
    void detectsARowWin() {
        Board board = new Board();
        board.place(new Position(0, 0), Symbol.X);
        board.place(new Position(0, 1), Symbol.X);
        board.place(new Position(0, 2), Symbol.X);

        assertThat(board.hasWinningLineFor(Symbol.X)).isTrue();
        assertThat(board.hasWinningLineFor(Symbol.O)).isFalse();
    }

    @Test
    void detectsAColumnWin() {
        Board board = new Board();
        board.place(new Position(0, 0), Symbol.O);
        board.place(new Position(1, 0), Symbol.O);
        board.place(new Position(2, 0), Symbol.O);

        assertThat(board.hasWinningLineFor(Symbol.O)).isTrue();
    }

    @Test
    void detectsADiagonalWin() {
        Board board = new Board();
        board.place(new Position(0, 0), Symbol.X);
        board.place(new Position(1, 1), Symbol.X);
        board.place(new Position(2, 2), Symbol.X);

        assertThat(board.hasWinningLineFor(Symbol.X)).isTrue();
    }

    @Test
    void detectsAnAntiDiagonalWin() {
        Board board = new Board();
        board.place(new Position(0, 2), Symbol.O);
        board.place(new Position(1, 1), Symbol.O);
        board.place(new Position(2, 0), Symbol.O);

        assertThat(board.hasWinningLineFor(Symbol.O)).isTrue();
    }

    @Test
    void reportsFullOnlyWhenEveryCellIsOccupied() {
        Board board = new Board();
        assertThat(board.isFull()).isFalse();

        for (int row = 0; row < Position.BOARD_SIZE; row++) {
            for (int col = 0; col < Position.BOARD_SIZE; col++) {
                board.place(new Position(row, col), (row + col) % 2 == 0 ? Symbol.X : Symbol.O);
            }
        }

        assertThat(board.isFull()).isTrue();
    }

    @Test
    void snapshotIsADefensiveCopy() {
        Board board = new Board();
        board.place(new Position(0, 0), Symbol.X);

        Symbol[][] snapshot = board.snapshotCells();
        snapshot[0][0] = Symbol.O;

        assertThat(board.snapshotCells()[0][0]).isEqualTo(Symbol.X);
    }
}
