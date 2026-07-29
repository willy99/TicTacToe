package com.flamingo.tictactoe.engine.domain.model;

/**
 * A read-only snapshot of a Game at one point in time. This is what gets
 * handed back to things like the web controller, so nobody outside can get
 * hold of the real, mutable game and change it.
 *
 * @param gameId the game id
 * @param cells  a copy of the board, cells[row][col], null means empty
 * @param status current game status
 * @param winner the winning symbol, only set when status is WIN
 */
public record GameSnapshot(String gameId, Symbol[][] cells, GameStatus status, Symbol winner) {
}
