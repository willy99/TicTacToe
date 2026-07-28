package com.flamingo.tictactoe.session.application.port.out;

import com.flamingo.tictactoe.session.domain.model.SessionStatus;
import com.flamingo.tictactoe.session.domain.model.Symbol;

/**
 * The subset of a Game Engine response the session domain actually needs:
 * whether the game is still in progress, and who (if anyone) won.
 */
public record EngineGameState(SessionStatus status, Symbol winner) {
}
