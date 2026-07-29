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
 * Application service implementing every use case exposed by the Game Session
 * Service. Orchestrates the session aggregate, the move generation strategy,
 * and the Game Engine client; contains no HTTP/JSON concerns of its own.
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
        // The session id doubles as the Game Engine's gameId, so the two
        // services always agree on which game a session refers to.
        String sessionId = UUID.randomUUID().toString();
        int boardSize = boardProperties.getSize();
        Session session = new Session(sessionId, boardSize);

        gameEngineClient.initializeGame(sessionId, boardSize);
        sessionRepository.save(session);

        return session.toSnapshot();
    }

    /**
     * Kicks off the automated simulation and returns immediately with the
     * session's current snapshot - it does not wait for the game to finish.
     * The loop that actually plays out the game runs on
     * {@link #simulationTaskExecutor}, so this call never blocks an HTTP
     * request thread for the several seconds a full game can take. Callers
     * that want to observe progress poll {@code GET /sessions/{id}}.
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
                // Each move is its own critical section (not the whole loop),
                // so a concurrent GET /sessions/{id} only ever blocks for the
                // duration of one move, not the whole game - that's what lets
                // a polling UI observe the board filling in move by move
                // instead of only seeing the final result.
                synchronized (session) {
                    playNextMove(session);
                    sessionRepository.save(session);
                    inProgress = session.isInProgress();
                }
            } catch (RuntimeException ex) {
                // Nothing is waiting on an HTTP response at this point - this
                // runs on the simulation executor, not a request thread - so
                // the only way a client learns about this is by polling and
                // seeing FAILED, same as it learns about WIN/DRAW.
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
        // Same monitor runSimulation() mutates under: Session's move list and
        // status are plain (non-thread-safe) fields, so a read must be
        // synchronized on the same lock as every write to avoid a torn read
        // or a ConcurrentModificationException while a simulation is running.
        synchronized (session) {
            return session.toSnapshot();
        }
    }

    private Session requireSession(String sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
    }
}
