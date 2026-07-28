package com.flamingo.tictactoe.session.domain.model;

import com.flamingo.tictactoe.session.domain.exception.SessionAlreadyCompletedException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Aggregate root for a single automated game session. Owns the move history
 * and tracks the outcome reported back by the Game Engine Service after each
 * move; contains no HTTP or move-generation logic of its own.
 */
public final class Session {

    private final String id;
    private final List<Move> moves = new ArrayList<>();
    private SessionStatus status = SessionStatus.IN_PROGRESS;
    private Symbol winner;

    public Session(String id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    /**
     * Records a move that was just accepted by the Game Engine Service and
     * updates the session's status to whatever the engine reported back.
     */
    public void recordMove(Symbol symbol, int row, int col, SessionStatus resultingStatus, Symbol resultingWinner) {
        if (status != SessionStatus.IN_PROGRESS) {
            throw new SessionAlreadyCompletedException(id, status);
        }
        moves.add(new Move(moves.size() + 1, symbol, row, col));
        this.status = resultingStatus;
        this.winner = resultingWinner;
    }

    public boolean isInProgress() {
        return status == SessionStatus.IN_PROGRESS;
    }

    /**
     * X always opens; players alternate from there based on how many moves
     * have been played so far.
     */
    public Symbol nextSymbol() {
        return moves.size() % 2 == 0 ? Symbol.X : Symbol.O;
    }

    public Set<Cell> occupiedCells() {
        Set<Cell> occupied = new HashSet<>();
        for (Move move : moves) {
            occupied.add(new Cell(move.row(), move.col()));
        }
        return occupied;
    }

    public String id() {
        return id;
    }

    public SessionStatus status() {
        return status;
    }

    public SessionSnapshot toSnapshot() {
        return new SessionSnapshot(id, status, winner, List.copyOf(moves));
    }
}
