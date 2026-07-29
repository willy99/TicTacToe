package com.flamingo.tictactoe.engine.application.port.in;

import com.flamingo.tictactoe.engine.domain.model.GameSnapshot;
import com.flamingo.tictactoe.engine.domain.model.Symbol;

/**
 * Plays one move on an existing game and returns the game's new state.
 */
public interface MakeMoveUseCase {

    GameSnapshot makeMove(String gameId, Symbol symbol, int row, int col);
}
