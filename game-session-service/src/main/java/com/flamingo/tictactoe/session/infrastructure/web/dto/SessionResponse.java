package com.flamingo.tictactoe.session.infrastructure.web.dto;

import java.util.List;

/**
 * Response body describing the current state of a session.
 *
 * @param sessionId the session identifier (also the Game Engine's gameId)
 * @param status    "IN_PROGRESS", "WIN", or "DRAW"
 * @param winner    "X" or "O" when {@code status == "WIN"}, {@code null} otherwise
 * @param moves     the full move history, in play order
 */
public record SessionResponse(String sessionId, String status, String winner, List<MoveDto> moves) {
}
