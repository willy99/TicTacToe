package com.flamingo.tictactoe.engine.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flamingo.tictactoe.engine.domain.exception.InvalidMoveException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class PositionTest {

    @Test
    void acceptsCoordinatesWithinTheBoard() {
        Position position = new Position(1, 2);

        assertThat(position.row()).isEqualTo(1);
        assertThat(position.col()).isEqualTo(2);
    }

    @ParameterizedTest
    @CsvSource({"-1,0", "0,-1", "3,0", "0,3", "10,10"})
    void rejectsCoordinatesOutsideTheBoard(int row, int col) {
        assertThatThrownBy(() -> new Position(row, col))
                .isInstanceOf(InvalidMoveException.class);
    }
}
