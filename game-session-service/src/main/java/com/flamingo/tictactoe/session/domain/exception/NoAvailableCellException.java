package com.flamingo.tictactoe.session.domain.exception;

/**
 * Defensive guard: raised if a move generation strategy is asked to pick a
 * cell on a board that is already full. Should never happen in practice since
 * {@code SessionService} stops simulating as soon as a session concludes.
 */
public class NoAvailableCellException extends RuntimeException {

    public NoAvailableCellException() {
        super("No free cell is available on the board");
    }
}
