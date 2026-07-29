package com.flamingo.tictactoe.engine.domain.model;

import com.flamingo.tictactoe.engine.domain.exception.GameAlreadyFinishedException;
import java.util.Objects;

/**
 * One Tic Tac Toe game. Holds the board and makes sure a move can only be
 * played while the game is still in progress; after every move it checks
 * whether that move won the game or filled the board for a draw.
 */
public final class Game {

    private final String id;
    private final Board board;
    private GameStatus status = GameStatus.IN_PROGRESS;
    private Symbol winner;

    /**
     * Creates a regular 3x3 game.
     */
    public Game(String id) {
        this(id, Board.DEFAULT_SIZE);
    }

    /**
     * Creates a game on a boardSize x boardSize board (must be at least 3).
     * Winning still means filling a whole row, column, or diagonal - just
     * scaled to whatever size the board is.
     */
    public Game(String id, int boardSize) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.board = new Board(boardSize);
    }

    /**
     * Plays a move for symbol at position, then checks whether it won or
     * drew the game. Throws if the game is already over or the move isn't allowed.
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
