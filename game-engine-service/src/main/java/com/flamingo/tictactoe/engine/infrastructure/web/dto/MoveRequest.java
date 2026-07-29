package com.flamingo.tictactoe.engine.infrastructure.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for {@code POST /games/{gameId}/move}.
 *
 * <p>Only non-negativity is validated here. There's no static upper bound on
 * {@code row}/{@code col} because a game's board size is configurable per
 * game (see {@code PUT /games/{gameId}?boardSize=}); whether a coordinate
 * actually fits *this* game's board is checked against its actual size by
 * the domain layer, which rejects an out-of-bounds move as an
 * {@code InvalidMoveException} (400).
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
