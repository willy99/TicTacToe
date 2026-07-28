package com.flamingo.tictactoe.session.domain.model;

/**
 * Overall outcome of a session's game, mirrored from the Game Engine Service's
 * response after each move.
 */
public enum SessionStatus {
    IN_PROGRESS,
    WIN,
    DRAW
}
