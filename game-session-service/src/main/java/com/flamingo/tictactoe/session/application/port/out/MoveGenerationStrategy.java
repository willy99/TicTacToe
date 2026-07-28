package com.flamingo.tictactoe.session.application.port.out;

import com.flamingo.tictactoe.session.domain.model.Cell;
import com.flamingo.tictactoe.session.domain.model.Symbol;
import java.util.Set;

/**
 * Outbound port for automated move generation (Strategy pattern). Swapping the
 * implementation bound to this interface - e.g. from random to rule-based or
 * minimax - requires no change to {@code SessionService}.
 */
public interface MoveGenerationStrategy {

    /**
     * Chooses the next cell to play for {@code symbol}.
     *
     * @param symbol        the player to move
     * @param occupiedCells cells already taken on the board
     * @param boardSize     the board's edge length (3 for a standard game)
     * @throws com.flamingo.tictactoe.session.domain.exception.NoAvailableCellException
     *         if every cell is already occupied
     */
    Cell nextMove(Symbol symbol, Set<Cell> occupiedCells, int boardSize);
}
