package com.flamingo.tictactoe.session.domain.exception;

/**
 * Raised if a move strategy is asked to pick a cell on a board that's
 * already full. Shouldn't actually happen, since SessionService stops
 * simulating as soon as a session is over.
 */
public class NoAvailableCellException extends RuntimeException {

    public NoAvailableCellException() {
        super("No free cell is available on the board");
    }
}
