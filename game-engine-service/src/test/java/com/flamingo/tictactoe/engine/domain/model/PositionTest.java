package com.flamingo.tictactoe.engine.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flamingo.tictactoe.engine.domain.exception.InvalidMoveException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class PositionTest {

    @Test
    void acceptsAnyNonNegativeCoordinate() {
        Position position = new Position(1, 2);

        assertThat(position.row()).isEqualTo(1);
        assertThat(position.col()).isEqualTo(2);
    }

    @Test
    void acceptsCoordinatesBeyondAClassicThreeByThreeBoard() {
        // Whether (10, 10) fits a given board is Board's concern (it depends
        // on that board's configured size), not Position's - see Board's
        // "requireWithinBounds" and BoardTest's larger-board coverage.
        Position position = new Position(10, 10);

        assertThat(position.row()).isEqualTo(10);
        assertThat(position.col()).isEqualTo(10);
    }

    @ParameterizedTest
    @CsvSource({"-1,0", "0,-1", "-1,-1"})
    void rejectsNegativeCoordinates(int row, int col) {
        assertThatThrownBy(() -> new Position(row, col))
                .isInstanceOf(InvalidMoveException.class);
    }
}
