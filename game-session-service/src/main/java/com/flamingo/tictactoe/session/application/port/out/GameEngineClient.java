package com.flamingo.tictactoe.session.application.port.out;

import com.flamingo.tictactoe.session.domain.model.Symbol;

/**
 * How the session service talks to the Game Engine. The rest of the code
 * only depends on this interface, not on the fact that it's HTTP
 * underneath - that detail lives in the class that implements it.
 */
public interface GameEngineClient {

    EngineGameState initializeGame(String gameId, int boardSize);

    EngineGameState submitMove(String gameId, Symbol symbol, int row, int col);
}
