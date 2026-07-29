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
    void aRejectedMoveDoesNotCorruptTheFilledCellCountUsedByIsFull() {
        Board board = new Board(3);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (row == 2 && col == 2) {
                    continue; // leave exactly one cell empty
                }
                board.place(new Position(row, col), Symbol.X);
            }
        }
        assertThat(board.isFull()).isFalse();

        // Repeatedly retrying an illegal move on an already-occupied cell must
        // never increment the internal filled-cell counter.
        assertThatThrownBy(() -> board.place(new Position(0, 0), Symbol.O))
                .isInstanceOf(CellOccupiedException.class);
        assertThat(board.isFull()).isFalse();

        board.place(new Position(2, 2), Symbol.O);
        assertThat(board.isFull()).isTrue();
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

        for (int row = 0; row < board.size(); row++) {
            for (int col = 0; col < board.size(); col++) {
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

    @Test
    void defaultConstructorProducesAClassicThreeByThreeBoard() {
        Board board = new Board();

        assertThat(board.size()).isEqualTo(3);
    }

    @Test
    void rejectsBoardSizesSmallerThanThree() {
        assertThatThrownBy(() -> new Board(2)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsAMoveOutsideAConfiguredLargerBoard() {
        Board board = new Board(6);

        assertThatThrownBy(() -> board.place(new Position(6, 0), Symbol.X))
                .isInstanceOf(com.flamingo.tictactoe.engine.domain.exception.InvalidMoveException.class);
    }

    @Test
    void detectsARowWinOnASixBySixBoard() {
        Board board = new Board(6);
        for (int col = 0; col < 6; col++) {
            board.place(new Position(2, col), Symbol.X);
        }

        assertThat(board.hasWinningLineFor(Symbol.X)).isTrue();
    }

    @Test
    void aFiveOutOfSixRowIsNotYetAWinOnASixBySixBoard() {
        Board board = new Board(6);
        for (int col = 0; col < 5; col++) {
            board.place(new Position(0, col), Symbol.X);
        }

        assertThat(board.hasWinningLineFor(Symbol.X)).isFalse();
    }

    @Test
    void detectsAColumnWinOnASixBySixBoard() {
        Board board = new Board(6);
        for (int row = 0; row < 6; row++) {
            board.place(new Position(row, 4), Symbol.O);
        }

        assertThat(board.hasWinningLineFor(Symbol.O)).isTrue();
    }

    @Test
    void detectsADiagonalWinOnASixBySixBoard() {
        Board board = new Board(6);
        for (int i = 0; i < 6; i++) {
            board.place(new Position(i, i), Symbol.X);
        }

        assertThat(board.hasWinningLineFor(Symbol.X)).isTrue();
    }

    @Test
    void detectsAnAntiDiagonalWinOnASixBySixBoard() {
        Board board = new Board(6);
        for (int i = 0; i < 6; i++) {
            board.place(new Position(i, 5 - i), Symbol.O);
        }

        assertThat(board.hasWinningLineFor(Symbol.O)).isTrue();
    }
}
