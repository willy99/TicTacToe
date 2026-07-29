package com.flamingo.tictactoe.session.domain.model;

import com.flamingo.tictactoe.session.domain.exception.SessionAlreadyCompletedException;
import com.flamingo.tictactoe.session.domain.exception.SessionSimulationAlreadyStartedException;
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
    private final int boardSize;
    private final List<Move> moves = new ArrayList<>();
    private SessionStatus status = SessionStatus.IN_PROGRESS;
    private Symbol winner;
    private String failureReason;
    private boolean simulationStarted;

    public Session(String id, int boardSize) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.boardSize = boardSize;
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

    /**
     * Marks the session as having its automated simulation under way. Guards
     * against two concurrent {@code POST /sessions/{id}/simulate} calls both
     * kicking off a background simulation for the same session - the second
     * caller gets a clear 409 instead of two independent workers racing to
     * play the same game.
     */
    public void startSimulation() {
        if (status != SessionStatus.IN_PROGRESS) {
            throw new SessionAlreadyCompletedException(id, status);
        }
        if (simulationStarted) {
            throw new SessionSimulationAlreadyStartedException(id);
        }
        simulationStarted = true;
    }

    /**
     * Marks the session as failed because the background simulation loop
     * couldn't reach the Game Engine. A no-op if the session already reached
     * a real outcome (WIN/DRAW) - that result takes precedence over a
     * failure discovered afterward.
     */
    public void markFailed(String reason) {
        if (status != SessionStatus.IN_PROGRESS) {
            return;
        }
        this.status = SessionStatus.FAILED;
        this.failureReason = reason;
    }

    public boolean isInProgress() {
        return status == SessionStatus.IN_PROGRESS;
    }

    public int boardSize() {
        return boardSize;
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
        return new SessionSnapshot(id, boardSize, status, winner, failureReason, List.copyOf(moves));
    }
}
