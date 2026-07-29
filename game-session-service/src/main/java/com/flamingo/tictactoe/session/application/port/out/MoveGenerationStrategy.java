package com.flamingo.tictactoe.session.application.port.out;

import com.flamingo.tictactoe.session.domain.model.Cell;
import com.flamingo.tictactoe.session.domain.model.Symbol;
import java.util.Set;

/**
 * How the next move gets picked for an automated player. Swapping this for
 * a different strategy (rule-based, minimax, etc.) doesn't require any
 * change to SessionService.
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
