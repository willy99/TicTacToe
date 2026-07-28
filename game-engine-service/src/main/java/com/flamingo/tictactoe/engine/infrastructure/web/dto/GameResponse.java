package com.flamingo.tictactoe.engine.infrastructure.web.dto;

import java.util.List;

/**
 * Response body describing the current state of a game.
 *
 * @param gameId the game identifier
 * @param board  3x3 grid, each cell is "X", "O", or {@code null} when empty
 * @param status "IN_PROGRESS", "WIN", or "DRAW"
 * @param winner "X" or "O" when {@code status == "WIN"}, {@code null} otherwise
 */
public record GameResponse(String gameId, List<List<String>> board, String status, String winner) {
}
