package com.flamingo.tictactoe.session.domain.model;

/**
 * Overall outcome of a session's game. {@code IN_PROGRESS}, {@code WIN}, and
 * {@code DRAW} are mirrored from the Game Engine Service's response after
 * each move; {@code FAILED} is set locally when the background simulation
 * loop can't reach the Game Engine at all, since that failure happens on a
 * background thread with no HTTP caller left to report it to directly - a
 * client only ever learns about it by polling {@code GET /sessions/{id}}.
 */
public enum SessionStatus {
    IN_PROGRESS,
    WIN,
    DRAW,
    FAILED
}
