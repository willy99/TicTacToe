package com.flamingo.tictactoe.session.infrastructure.client.dto;

import java.util.List;

/**
 * The JSON body the Game Engine's /games/{gameId} and
 * /games/{gameId}/move endpoints return.
 */
public record EngineGameResponseDto(String gameId, List<List<String>> board, String status, String winner) {
}
