package com.flamingo.tictactoe.session.application.port.out;

import com.flamingo.tictactoe.session.domain.model.Symbol;

/**
 * Outbound port abstracting communication with the Game Engine Service.
 * The application layer depends only on this interface; the HTTP transport
 * detail lives entirely in the infrastructure adapter that implements it.
 */
public interface GameEngineClient {

    EngineGameState initializeGame(String gameId, int boardSize);

    EngineGameState submitMove(String gameId, Symbol symbol, int row, int col);
}
