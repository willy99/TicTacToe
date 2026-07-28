package com.flamingo.tictactoe.session.infrastructure.strategy;

import com.flamingo.tictactoe.session.application.port.out.MoveGenerationStrategy;
import com.flamingo.tictactoe.session.domain.exception.NoAvailableCellException;
import com.flamingo.tictactoe.session.domain.model.Cell;
import com.flamingo.tictactoe.session.domain.model.Symbol;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

/**
 * Default {@link MoveGenerationStrategy}: picks uniformly at random among the
 * cells that are still free. Simple by design, per the assignment's "simple
 * random or rule-based algorithm" requirement, and fully swappable behind the port.
 */
@Component
public class RandomMoveGenerationStrategy implements MoveGenerationStrategy {

    @Override
    public Cell nextMove(Symbol symbol, Set<Cell> occupiedCells, int boardSize) {
        List<Cell> availableCells = new ArrayList<>();
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Cell candidate = new Cell(row, col);
                if (!occupiedCells.contains(candidate)) {
                    availableCells.add(candidate);
                }
            }
        }

        if (availableCells.isEmpty()) {
            throw new NoAvailableCellException();
        }

        int index = ThreadLocalRandom.current().nextInt(availableCells.size());
        return availableCells.get(index);
    }
}
