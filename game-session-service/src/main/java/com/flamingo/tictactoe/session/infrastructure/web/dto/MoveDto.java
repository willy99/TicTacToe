package com.flamingo.tictactoe.session.infrastructure.web.dto;

/**
 * A single entry in a session's move history, as returned to API clients.
 */
public record MoveDto(int moveNumber, String symbol, int row, int col) {
}
