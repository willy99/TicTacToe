package com.flamingo.tictactoe.engine.domain.model;

import com.flamingo.tictactoe.engine.domain.exception.GameAlreadyFinishedException;
import java.util.Objects;

/**
 * Aggregate root for a single Tic Tac Toe game. Owns the board and enforces every
 * invariant: a move may only be applied while the game is in progress, and the
 * outcome (win/draw/in-progress) is re-evaluated after each move.
 */
public final class Game {

    private final String id;
    private final Board board;
    private GameStatus status = GameStatus.IN_PROGRESS;
    private Symbol winner;

    /**
     * Creates a classic 3x3 game. Equivalent to {@code new Game(id, Board.DEFAULT_SIZE)}.
     */
    public Game(String id) {
        this(id, Board.DEFAULT_SIZE);
    }

    /**
     * Creates a game on a {@code boardSize x boardSize} board (must be at
     * least 3). Winning still means filling an entire row, column, or
     * diagonal - just scaled to the board's own size.
     */
    public Game(String id, int boardSize) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.board = new Board(boardSize);
    }

    /**
     * Applies a move for {@code symbol} at {@code position}, then re-evaluates the
     * game outcome. Throws if the game already finished or the move is illegal.
     */
    public void applyMove(Symbol symbol, Position position) {
        if (status != GameStatus.IN_PROGRESS) {
            throw new GameAlreadyFinishedException(id, status);
        }

        board.place(position, symbol);
        evaluateOutcome(symbol);
    }

    private void evaluateOutcome(Symbol lastPlayed) {
        if (board.hasWinningLineFor(lastPlayed)) {
            status = GameStatus.WIN;
            winner = lastPlayed;
        } else if (board.isFull()) {
            status = GameStatus.DRAW;
        }
    }

    public String id() {
        return id;
    }

    public GameStatus status() {
        return status;
    }

    public GameSnapshot toSnapshot() {
        return new GameSnapshot(id, board.snapshotCells(), status, winner);
    }
}
