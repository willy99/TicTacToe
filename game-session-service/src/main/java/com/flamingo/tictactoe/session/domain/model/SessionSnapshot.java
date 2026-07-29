package com.flamingo.tictactoe.session.domain.model;

import java.util.List;

/**
 * A read-only snapshot of a Session at one point in time, handed out so
 * nobody outside can get hold of the real, mutable session and change it.
 */
public record SessionSnapshot(
        String sessionId,
        int boardSize,
        SessionStatus status,
        Symbol winner,
        String failureReason,
        List<Move> moves) {
}
