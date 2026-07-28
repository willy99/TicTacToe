package com.flamingo.tictactoe.session.domain.model;

/**
 * A zero-based (row, column) coordinate on the 3x3 board. Used to describe
 * occupied cells and the move a {@code MoveGenerationStrategy} chooses next.
 */
public record Cell(int row, int col) {
}
