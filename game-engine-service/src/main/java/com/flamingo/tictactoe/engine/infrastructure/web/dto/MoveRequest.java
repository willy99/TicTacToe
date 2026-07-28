package com.flamingo.tictactoe.engine.infrastructure.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for {@code POST /games/{gameId}/move}.
 *
 * @param symbol "X" or "O"
 * @param row    zero-based row, 0-2
 * @param col    zero-based column, 0-2
 */
public record MoveRequest(

        @NotNull(message = "symbol is required")
        @Pattern(regexp = "^[XO]$", message = "symbol must be 'X' or 'O'")
        String symbol,

        @NotNull(message = "row is required")
        @Min(value = 0, message = "row must be between 0 and 2")
        @Max(value = 2, message = "row must be between 0 and 2")
        Integer row,

        @NotNull(message = "col is required")
        @Min(value = 0, message = "col must be between 0 and 2")
        @Max(value = 2, message = "col must be between 0 and 2")
        Integer col
) {
}
