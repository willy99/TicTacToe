package com.flamingo.tictactoe.session.infrastructure.persistence.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * A first sketch of what a saved session row would look like with JPA/H2
 * instead of the in-memory map. Not wired into the app yet -
 * SessionRepository is still backed by InMemorySessionRepository - this is
 * just here to show the shape a real persistence layer would take.
 *
 * <p>It only stores the session's summary (id, size, status, winner,
 * failure reason), not the move history. Persisting moves would need their
 * own table (a session has many moves) and is the next step if this
 * direction gets picked up for real.
 */
@Entity
public class SessionEntity {

    @Id
    private String id;
    private int boardSize;
    private String status;
    private String winner;
    private String failureReason;

    protected SessionEntity() {
        // JPA needs a no-args constructor
    }

    public SessionEntity(String id, int boardSize, String status, String winner, String failureReason) {
        this.id = id;
        this.boardSize = boardSize;
        this.status = status;
        this.winner = winner;
        this.failureReason = failureReason;
    }

    public String getId() {
        return id;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public String getStatus() {
        return status;
    }

    public String getWinner() {
        return winner;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
