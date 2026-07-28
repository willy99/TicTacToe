package com.flamingo.tictactoe.engine.application.port.in;

import com.flamingo.tictactoe.engine.domain.model.GameSnapshot;
import com.flamingo.tictactoe.engine.domain.model.Symbol;

/**
 * Inbound port: apply a single move to an existing game and return the resulting state.
 */
public interface MakeMoveUseCase {

    GameSnapshot makeMove(String gameId, Symbol symbol, int row, int col);
}
