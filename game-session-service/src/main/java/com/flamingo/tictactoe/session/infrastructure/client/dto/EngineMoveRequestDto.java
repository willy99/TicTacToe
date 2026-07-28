package com.flamingo.tictactoe.session.infrastructure.client.dto;

/**
 * Wire-level request body sent to {@code POST /games/{gameId}/move} on the
 * Game Engine Service.
 */
public record EngineMoveRequestDto(String symbol, int row, int col) {
}
