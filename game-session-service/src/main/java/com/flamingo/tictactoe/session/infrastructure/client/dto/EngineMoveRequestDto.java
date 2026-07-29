package com.flamingo.tictactoe.session.infrastructure.client.dto;

/**
 * The JSON body sent to POST /games/{gameId}/move on the Game Engine.
 */
public record EngineMoveRequestDto(String symbol, int row, int col) {
}
