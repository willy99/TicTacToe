package com.flamingo.tictactoe.session.infrastructure.client.dto;

import java.util.List;

/**
 * Wire-level response body returned by the Game Engine Service's
 * {@code /games/{gameId}} and {@code /games/{gameId}/move} endpoints.
 */
public record EngineGameResponseDto(String gameId, List<List<String>> board, String status, String winner) {
}
