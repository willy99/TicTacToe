package com.flamingo.tictactoe.engine.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flamingo.tictactoe.engine.domain.exception.GameAlreadyFinishedException;
import org.junit.jupiter.api.Test;

class GameTest {

    @Test
    void startsInProgressWithNoWinner() {
        Game game = new Game("g1");

        GameSnapshot snapshot = game.toSnapshot();

        assertThat(snapshot.status()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(snapshot.winner()).isNull();
    }

    @Test
    void movingToAWinningLineEndsTheGameWithAWinner() {
        Game game = new Game("g1");

        game.applyMove(Symbol.X, new Position(0, 0));
        game.applyMove(Symbol.O, new Position(1, 0));
        game.applyMove(Symbol.X, new Position(0, 1));
        game.applyMove(Symbol.O, new Position(1, 1));
        game.applyMove(Symbol.X, new Position(0, 2)); // X completes the top row

        GameSnapshot snapshot = game.toSnapshot();
        assertThat(snapshot.status()).isEqualTo(GameStatus.WIN);
        assertThat(snapshot.winner()).isEqualTo(Symbol.X);
    }

    @Test
    void fillingTheBoardWithNoWinnerIsADraw() {
        Game game = new Game("g1");
        // X O X
        // X O O
        // O X X
        game.applyMove(Symbol.X, new Position(0, 0));
        game.applyMove(Symbol.O, new Position(0, 1));
        game.applyMove(Symbol.X, new Position(0, 2));
        game.applyMove(Symbol.O, new Position(1, 1));
        game.applyMove(Symbol.X, new Position(1, 0));
        game.applyMove(Symbol.O, new Position(1, 2));
        game.applyMove(Symbol.X, new Position(2, 1));
        game.applyMove(Symbol.O, new Position(2, 0));
        game.applyMove(Symbol.X, new Position(2, 2));

        GameSnapshot snapshot = game.toSnapshot();
        assertThat(snapshot.status()).isEqualTo(GameStatus.DRAW);
        assertThat(snapshot.winner()).isNull();
    }

    @Test
    void rejectsAMoveOnceTheGameHasFinished() {
        Game game = new Game("g1");
        game.applyMove(Symbol.X, new Position(0, 0));
        game.applyMove(Symbol.O, new Position(1, 0));
        game.applyMove(Symbol.X, new Position(0, 1));
        game.applyMove(Symbol.O, new Position(1, 1));
        game.applyMove(Symbol.X, new Position(0, 2)); // X wins

        assertThatThrownBy(() -> game.applyMove(Symbol.O, new Position(2, 2)))
                .isInstanceOf(GameAlreadyFinishedException.class);
    }

    @Test
    void supportsALargerConfiguredBoardSize() {
        Game game = new Game("g1", 4);

        game.applyMove(Symbol.X, new Position(0, 0));
        game.applyMove(Symbol.O, new Position(1, 0));
        game.applyMove(Symbol.X, new Position(0, 1));
        game.applyMove(Symbol.O, new Position(1, 1));
        game.applyMove(Symbol.X, new Position(0, 2));
        game.applyMove(Symbol.O, new Position(1, 2));
        // Three in a row is not a win on a 4x4 board - the game must still be in progress.
        assertThat(game.toSnapshot().status()).isEqualTo(GameStatus.IN_PROGRESS);

        game.applyMove(Symbol.X, new Position(0, 3)); // X completes all 4 cells of the top row

        GameSnapshot snapshot = game.toSnapshot();
        assertThat(snapshot.status()).isEqualTo(GameStatus.WIN);
        assertThat(snapshot.winner()).isEqualTo(Symbol.X);
        assertThat(snapshot.cells()).hasDimensions(4, 4);
    }
}
