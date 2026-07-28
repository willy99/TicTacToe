package com.flamingo.tictactoe.engine.domain.model;

/**
 * Immutable, read-only view of a {@link Game} at a point in time. This is the shape
 * the application layer hands back to adapters (web controllers, etc.) so that the
 * mutable {@link Game} aggregate never leaks outside the domain.
 *
 * @param gameId the game identifier
 * @param cells  a defensive copy of the board, {@code cells[row][col]}, {@code null} means empty
 * @param status current game status
 * @param winner the winning symbol, present only when {@code status == GameStatus.WIN}
 */
public record GameSnapshot(String gameId, Symbol[][] cells, GameStatus status, Symbol winner) {
}
