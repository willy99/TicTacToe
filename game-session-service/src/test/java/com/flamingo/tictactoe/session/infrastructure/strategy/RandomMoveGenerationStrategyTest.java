package com.flamingo.tictactoe.session.infrastructure.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flamingo.tictactoe.session.domain.exception.NoAvailableCellException;
import com.flamingo.tictactoe.session.domain.model.Cell;
import com.flamingo.tictactoe.session.domain.model.Symbol;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

class RandomMoveGenerationStrategyTest {

    private final RandomMoveGenerationStrategy strategy = new RandomMoveGenerationStrategy();

    @RepeatedTest(20)
    void alwaysReturnsAFreeCell() {
        Set<Cell> occupied = Set.of(new Cell(0, 0), new Cell(1, 1));

        Cell chosen = strategy.nextMove(Symbol.X, occupied, 3);

        assertThat(occupied).doesNotContain(chosen);
        assertThat(chosen.row()).isBetween(0, 2);
        assertThat(chosen.col()).isBetween(0, 2);
    }

    @Test
    void returnsTheOnlyRemainingCellWhenJustOneIsFree() {
        Set<Cell> occupied = Set.of(
                new Cell(0, 0), new Cell(0, 1), new Cell(0, 2),
                new Cell(1, 0), new Cell(1, 1), new Cell(1, 2),
                new Cell(2, 0), new Cell(2, 1));

        Cell chosen = strategy.nextMove(Symbol.O, occupied, 3);

        assertThat(chosen).isEqualTo(new Cell(2, 2));
    }

    @Test
    void throwsWhenTheBoardIsFull() {
        Set<Cell> occupied = Set.of(
                new Cell(0, 0), new Cell(0, 1), new Cell(0, 2),
                new Cell(1, 0), new Cell(1, 1), new Cell(1, 2),
                new Cell(2, 0), new Cell(2, 1), new Cell(2, 2));

        assertThatThrownBy(() -> strategy.nextMove(Symbol.X, occupied, 3))
                .isInstanceOf(NoAvailableCellException.class);
    }
}
