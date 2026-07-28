package com.flamingo.tictactoe.session.domain.model;

/**
 * A single recorded move in a session's history, in the order it was played.
 */
public record Move(int moveNumber, Symbol symbol, int row, int col) {
}
