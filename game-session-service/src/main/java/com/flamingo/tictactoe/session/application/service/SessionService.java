package com.flamingo.tictactoe.session.application.service;

import com.flamingo.tictactoe.session.application.port.in.CreateSessionUseCase;
import com.flamingo.tictactoe.session.application.port.in.GetSessionUseCase;
import com.flamingo.tictactoe.session.application.port.in.SimulateGameUseCase;
import com.flamingo.tictactoe.session.application.port.out.EngineGameState;
import com.flamingo.tictactoe.session.application.port.out.GameEngineClient;
import com.flamingo.tictactoe.session.application.port.out.MoveGenerationStrategy;
import com.flamingo.tictactoe.session.application.port.out.SessionRepository;
import com.flamingo.tictactoe.session.domain.exception.SessionNotFoundException;
import com.flamingo.tictactoe.session.domain.model.Cell;
import com.flamingo.tictactoe.session.domain.model.Session;
import com.flamingo.tictactoe.session.domain.model.SessionSnapshot;
import com.flamingo.tictactoe.session.domain.model.Symbol;
import com.flamingo.tictactoe.session.infrastructure.config.BoardProperties;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

/**
 * Handles the session service's actions: create a session, run the
 * simulation, read a session's state. Ties together the session itself,
 * the move-picking strategy, and the Game Engine client - no HTTP/JSON
 * code here, that's in the web controller.
 */
@Service
public class SessionService implements CreateSessionUseCase, SimulateGameUseCase, GetSessionUseCase {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository sessionRepository;
    private final GameEngineClient gameEngineClient;
    private final MoveGenerationStrategy moveGenerationStrategy;
    private final BoardProperties boardProperties;
    private final TaskExecutor simulationTaskExecutor;
    private final long moveDelayMillis;

    public SessionService(SessionRepository sessionRepository,
                           GameEngineClient gameEngineClient,
                           MoveGenerationStrategy moveGenerationStrategy,
                           BoardProperties boardProperties,
                           @Qualifier("simulationTaskExecutor") TaskExecutor simulationTaskExecutor,
                           @Value("${simulation.move-delay-ms:500}") long moveDelayMillis) {
        this.sessionRepository = sessionRepository;
        this.gameEngineClient = gameEngineClient;
        this.moveGenerationStrategy = moveGenerationStrategy;
        this.boardProperties = boardProperties;
        this.simulationTaskExecutor = simulationTaskExecutor;
        this.moveDelayMillis = moveDelayMillis;
    }

    @Override
    public SessionSnapshot createSession() {
        // The session id is also used as the Game Engine's gameId, so both
        // services always agree on which game a session refers to.
        String sessionId = UUID.randomUUID().toString();
        int boardSize = boardProperties.getSize();
        Session session = new Session(sessionId, boardSize);

        gameEngineClient.initializeGame(sessionId, boardSize);
        sessionRepository.save(session);

        return session.toSnapshot();
    }

    /**
     * Starts the automated simulation and returns right away with the
     * session's current snapshot - it doesn't wait for the game to finish.
     * The loop that actually plays the game runs on simulationTaskExecutor,
     * so this call never blocks an HTTP request thread for the several
     * seconds a full game can take. Poll GET /sessions/{id} to see progress.
     */
    @Override
    public SessionSnapshot simulate(String sessionId) {
        Session session = requireSession(sessionId);

        synchronized (session) {
            session.startSimulation();
        }

        simulationTaskExecutor.execute(() -> runSimulation(session));

        return snapshotOf(session);
    }

    private void runSimulation(Session session) {
        boolean inProgress = true;
        while (inProgress) {
            try {
                // Each move locks the session on its own, not the whole
                // loop, so a GET /sessions/{id} request only ever has to
                // wait for one move to finish, not the whole game. That's
                // what lets a polling UI see the board fill in move by
                // move instead of only the final result.
                synchronized (session) {
                    playNextMove(session);
                    sessionRepository.save(session);
                    inProgress = session.isInProgress();
                }
            } catch (RuntimeException ex) {
                // There's no HTTP request waiting right now - this runs in
                // the background, not on a request thread - so polling and
                // seeing FAILED is the only way a client finds out about
                // this, same as it finds out about WIN/DRAW.
                log.error("Simulation failed for session {}", session.id(), ex);
                synchronized (session) {
                    session.markFailed(ex.getMessage());
                    sessionRepository.save(session);
                }
                return;
            }
            if (inProgress) {
                pauseBetweenMoves();
            }
        }
    }

    private void playNextMove(Session session) {
        Symbol symbol = session.nextSymbol();
        Cell cell = moveGenerationStrategy.nextMove(symbol, session.occupiedCells(), session.boardSize());

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
        return snapshotOf(requireSession(sessionId));
    }

    private SessionSnapshot snapshotOf(Session session) {
        // Locks on the same session object runSimulation() uses. Session's
        // move list and status are plain fields, not thread-safe, so a
        // read has to use the same lock as every write - otherwise this
        // could read a half-updated session while a simulation is running.
        synchronized (session) {
            return session.toSnapshot();
        }
    }

    private Session requireSession(String sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
    }
}
