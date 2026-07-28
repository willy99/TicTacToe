package com.flamingo.tictactoe.session.domain.model;

import java.util.List;

/**
 * Immutable, read-only view of a {@link Session} at a point in time, handed
 * out by the application layer so the mutable aggregate never leaks outside
 * the domain.
 */
public record SessionSnapshot(String sessionId, SessionStatus status, Symbol winner, List<Move> moves) {
}
