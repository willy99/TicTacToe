package com.flamingo.tictactoe.session.infrastructure.web.dto;

import java.util.List;

/**
 * Response body describing the current state of a session.
 *
 * @param sessionId     the session identifier (also the Game Engine's gameId)
 * @param boardSize     the edge length of the board this session is played on
 * @param status        "IN_PROGRESS", "WIN", "DRAW", or "FAILED"
 * @param winner        "X" or "O" when {@code status == "WIN"}, {@code null} otherwise
 * @param failureReason set when {@code status == "FAILED"}, {@code null} otherwise
 * @param moves         the full move history, in play order
 */
public record SessionResponse(
        String sessionId,
        int boardSize,
        String status,
        String winner,
        String failureReason,
        List<MoveDto> moves) {
}
