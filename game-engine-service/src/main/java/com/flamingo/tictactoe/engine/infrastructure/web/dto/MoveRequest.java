package com.flamingo.tictactoe.engine.infrastructure.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for POST /games/{gameId}/move.
 *
 * <p>Only checks that row/col aren't negative. There's no upper limit here
 * because board size can be different for each game (see
 * PUT /games/{gameId}?boardSize=) - whether a move actually fits on this
 * particular game's board gets checked afterward, and rejected as an
 * InvalidMoveException (400) if it doesn't.
 *
 * @param symbol "X" or "O"
 * @param row    zero-based row
 * @param col    zero-based column
 */
public record MoveRequest(

        @NotNull(message = "symbol is required")
        @Pattern(regexp = "^[XO]$", message = "symbol must be 'X' or 'O'")
        String symbol,

        @NotNull(message = "row is required")
        @Min(value = 0, message = "row must not be negative")
        Integer row,

        @NotNull(message = "col is required")
        @Min(value = 0, message = "col must not be negative")
        Integer col
) {
}
