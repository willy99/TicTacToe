package com.flamingo.tictactoe.engine.infrastructure.persistence.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * A first sketch of what a saved game row would look like with JPA/H2
 * instead of the in-memory map. Not wired into the app yet -
 * GameRepository is still backed by InMemoryGameRepository - this is just
 * here to show the shape a real persistence layer would take.
 *
 * <p>It only stores the game's summary (id, size, status, winner), not the
 * board cells themselves. Persisting the full board would need its own
 * table (or a JSON column) and is the next step if this direction gets
 * picked up for real.
 */
@Entity
public class GameEntity {

    @Id
    private String id;
    private int boardSize;
    private String status;
    private String winner;

    protected GameEntity() {
        // JPA needs a no-args constructor
    }

    public GameEntity(String id, int boardSize, String status, String winner) {
        this.id = id;
        this.boardSize = boardSize;
        this.status = status;
        this.winner = winner;
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
}
