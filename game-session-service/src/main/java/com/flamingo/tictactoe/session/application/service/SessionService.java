package com.flamingo.tictactoe.session.application.service;

import com.flamingo.tictactoe.session.application.port.in.CreateSessionUseCase;
import com.flamingo.tictactoe.session.application.port.in.GetSessionUseCase;
import com.flamingo.tictactoe.session.application.port.in.SimulateGameUseCase;
import com.flamingo.tictactoe.session.application.port.out.EngineGameState;
import com.flamingo.tictactoe.session.application.port.out.GameEngineClient;
import com.flamingo.tictactoe.session.application.port.out.MoveGenerationStrategy;
import com.flamingo.tictactoe.session.application.port.out.SessionRepository;
import com.flamingo.tictactoe.session.domain.exception.SessionAlreadyCompletedException;
import com.flamingo.tictactoe.session.domain.exception.SessionNotFoundException;
import com.flamingo.tictactoe.session.domain.model.Cell;
import com.flamingo.tictactoe.session.domain.model.Session;
import com.flamingo.tictactoe.session.domain.model.SessionSnapshot;
import com.flamingo.tictactoe.session.domain.model.Symbol;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Application service implementing every use case exposed by the Game Session
 * Service. Orchestrates the session aggregate, the move generation strategy,
 * and the Game Engine client; contains no HTTP/JSON concerns of its own.
 */
@Service
public class SessionService implements CreateSessionUseCase, SimulateGameUseCase, GetSessionUseCase {

    private static final int BOARD_SIZE = 3;

    private final SessionRepository sessionRepository;
    private final GameEngineClient gameEngineClient;
    private final MoveGenerationStrategy moveGenerationStrategy;
    private final long moveDelayMillis;

    public SessionService(SessionRepository sessionRepository,
                           GameEngineClient gameEngineClient,
                           MoveGenerationStrategy moveGenerationStrategy,
                           @Value("${simulation.move-delay-ms:500}") long moveDelayMillis) {
        this.sessionRepository = sessionRepository;
        this.gameEngineClient = gameEngineClient;
        this.moveGenerationStrategy = moveGenerationStrategy;
        this.moveDelayMillis = moveDelayMillis;
    }

    @Override
    public SessionSnapshot createSession() {
        // The session id doubles as the Game Engine's gameId, so the two
        // services always agree on which game a session refers to.
        String sessionId = UUID.randomUUID().toString();
        Session session = new Session(sessionId);

        gameEngineClient.initializeGame(sessionId);
        sessionRepository.save(session);

        return session.toSnapshot();
    }

    @Override
    public SessionSnapshot simulate(String sessionId) {
        Session session = requireSession(sessionId);

        // Synchronize per-session-instance so two concurrent simulate calls for
        // the same session cannot interleave their moves against the engine.
        synchronized (session) {
            if (!session.isInProgress()) {
                throw new SessionAlreadyCompletedException(sessionId, session.status());
            }

            while (session.isInProgress()) {
                playNextMove(session);
                // Persist after every move (not just at the end) so a concurrent
                // GET /sessions/{id} - e.g. a UI polling loop - observes the game
                // progressing move by move while this call is still running.
                sessionRepository.save(session);
                pauseBetweenMoves();
            }

            return session.toSnapshot();
        }
    }

    private void playNextMove(Session session) {
        Symbol symbol = session.nextSymbol();
        Cell cell = moveGenerationStrategy.nextMove(symbol, session.occupiedCells(), BOARD_SIZE);

        EngineGameState outcome = gameEngineClient.submitMove(session.id(), symbol, cell.row(), cell.col());

        session.recordMove(symbol, cell.row(), cell.col(), outcome.status(), outcome.winner());
    }

    private void pauseBetweenMoves() {
        if (moveDelayMillis <= 0) {
            return;
        }
        try {
            Thread.sleep(moveDelayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public SessionSnapshot getSession(String sessionId) {
        return requireSession(sessionId).toSnapshot();
    }

    private Session requireSession(String sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
    }
}
