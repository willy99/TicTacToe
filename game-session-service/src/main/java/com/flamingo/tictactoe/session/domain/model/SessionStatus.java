package com.flamingo.tictactoe.session.domain.model;

/**
 * The outcome of a session's game. IN_PROGRESS, WIN, and DRAW are just
 * copied from what the Game Engine reports after each move. FAILED is set
 * locally when the background simulation can't reach the Game Engine at
 * all - that happens on a background thread with nobody waiting for an
 * HTTP response, so a client only finds out by polling GET /sessions/{id}.
 */
public enum SessionStatus {
    IN_PROGRESS,
    WIN,
    DRAW,
    FAILED
}
