package com.flamingo.tictactoe.session.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flamingo.tictactoe.session.application.port.out.EngineGameState;
import com.flamingo.tictactoe.session.application.port.out.GameEngineClient;
import com.flamingo.tictactoe.session.application.port.out.MoveGenerationStrategy;
import com.flamingo.tictactoe.session.application.port.out.SessionRepository;
import com.flamingo.tictactoe.session.application.port.out.SessionUpdatePublisher;
import com.flamingo.tictactoe.session.domain.exception.GameEngineCommunicationException;
import com.flamingo.tictactoe.session.domain.exception.SessionAlreadyCompletedException;
import com.flamingo.tictactoe.session.domain.exception.SessionNotFoundException;
import com.flamingo.tictactoe.session.domain.exception.SessionSimulationAlreadyStartedException;
import com.flamingo.tictactoe.session.domain.model.Cell;
import com.flamingo.tictactoe.session.domain.model.Session;
import com.flamingo.tictactoe.session.domain.model.SessionSnapshot;
import com.flamingo.tictactoe.session.domain.model.SessionStatus;
import com.flamingo.tictactoe.session.domain.model.Symbol;
import com.flamingo.tictactoe.session.infrastructure.config.BoardProperties;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private GameEngineClient gameEngineClient;
    @Mock
    private MoveGenerationStrategy moveGenerationStrategy;
    @Mock
    private SessionUpdatePublisher sessionUpdatePublisher;

    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        BoardProperties boardProperties = new BoardProperties();
        boardProperties.setSize(3);
        // SyncTaskExecutor runs the simulation on the same thread instead
        // of a real background worker, so simulate() only returns after
        // the whole game is done - that keeps these tests simple and
        // predictable.
        sessionService = new SessionService(
                sessionRepository, gameEngineClient, moveGenerationStrategy,
                boardProperties, new SyncTaskExecutor(), sessionUpdatePublisher, 0L);
    }

    @Test
    void createSessionInitializesTheGameEngineAndPersistsTheSession() {
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SessionSnapshot snapshot = sessionService.createSession();

        assertThat(snapshot.status()).isEqualTo(SessionStatus.IN_PROGRESS);
        assertThat(snapshot.moves()).isEmpty();
        assertThat(snapshot.boardSize()).isEqualTo(3);
        verify(gameEngineClient).initializeGame(snapshot.sessionId(), 3);
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void simulatePlaysAlternatingMovesUntilTheGameConcludes() {
        Session session = new Session("s1", 3);
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // X plays (0,0) -> in progress, O plays (1,1) -> in progress, X plays (0,1) -> win
        when(moveGenerationStrategy.nextMove(eq(Symbol.X), any(), anyInt()))
                .thenReturn(new Cell(0, 0), new Cell(0, 1));
        when(moveGenerationStrategy.nextMove(eq(Symbol.O), any(), anyInt()))
                .thenReturn(new Cell(1, 1));

        when(gameEngineClient.submitMove(eq("s1"), eq(Symbol.X), eq(0), eq(0)))
                .thenReturn(new EngineGameState(SessionStatus.IN_PROGRESS, null));
        when(gameEngineClient.submitMove(eq("s1"), eq(Symbol.O), eq(1), eq(1)))
                .thenReturn(new EngineGameState(SessionStatus.IN_PROGRESS, null));
        when(gameEngineClient.submitMove(eq("s1"), eq(Symbol.X), eq(0), eq(1)))
                .thenReturn(new EngineGameState(SessionStatus.WIN, Symbol.X));

        SessionSnapshot snapshot = sessionService.simulate("s1");

        assertThat(snapshot.status()).isEqualTo(SessionStatus.WIN);
        assertThat(snapshot.winner()).isEqualTo(Symbol.X);
        assertThat(snapshot.moves()).hasSize(3);
        // Saved once per move (3 moves) so a concurrent poller sees live progress.
        verify(sessionRepository, org.mockito.Mockito.times(3)).save(session);
        // Published once per move too, so anyone watching the stream sees
        // the same live progress.
        verify(sessionUpdatePublisher, org.mockito.Mockito.times(3)).publish(any());
    }

    @Test
    void simulateThrowsWhenSessionDoesNotExist() {
        when(sessionRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.simulate("missing"))
                .isInstanceOf(SessionNotFoundException.class);
    }

    @Test
    void simulateThrowsWhenSessionAlreadyCompleted() {
        Session session = new Session("s1", 3);
        session.recordMove(Symbol.X, 0, 0, SessionStatus.WIN, Symbol.X);
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> sessionService.simulate("s1"))
                .isInstanceOf(SessionAlreadyCompletedException.class);
    }

    @Test
    void getSessionThrowsWhenSessionDoesNotExist() {
        when(sessionRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.getSession("missing"))
                .isInstanceOf(SessionNotFoundException.class);
    }

    @Test
    void getSessionReturnsCurrentSnapshot() {
        Session session = new Session("s1", 3);
        session.recordMove(Symbol.X, 0, 0, SessionStatus.IN_PROGRESS, null);
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(session));

        SessionSnapshot snapshot = sessionService.getSession("s1");

        assertThat(snapshot.moves()).hasSize(1);
    }

    @Test
    void simulateThrowsWhenSimulationAlreadyStartedConcurrently() {
        Session session = new Session("s1", 3);
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(session));

        BoardProperties boardProperties = new BoardProperties();
        boardProperties.setSize(3);
        // This executor never actually runs the queued simulation, so the
        // session stays marked "started" but still in progress - exactly
        // the situation where a second call should get rejected.
        TaskExecutor noOpExecutor = mock(TaskExecutor.class);
        SessionService service = new SessionService(
                sessionRepository, gameEngineClient, moveGenerationStrategy,
                boardProperties, noOpExecutor, sessionUpdatePublisher, 0L);

        service.simulate("s1");

        assertThatThrownBy(() -> service.simulate("s1"))
                .isInstanceOf(SessionSimulationAlreadyStartedException.class);
    }

    @Test
    void simulateMarksTheSessionFailedWhenTheEngineIsUnreachable() {
        Session session = new Session("s1", 3);
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(moveGenerationStrategy.nextMove(eq(Symbol.X), any(), anyInt())).thenReturn(new Cell(0, 0));
        when(gameEngineClient.submitMove(any(), any(), anyInt(), anyInt()))
                .thenThrow(new GameEngineCommunicationException("Unable to reach the Game Engine Service", null));

        SessionSnapshot snapshot = sessionService.simulate("s1");

        assertThat(snapshot.status()).isEqualTo(SessionStatus.FAILED);
        assertThat(snapshot.failureReason()).contains("Unable to reach the Game Engine Service");
        verify(sessionUpdatePublisher).publish(snapshot);
    }

    @Test
    void simulateNeverCallsTheStrategyWithAlreadyOccupiedCells() {
        Session session = new Session("s1", 3);
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(moveGenerationStrategy.nextMove(eq(Symbol.X), eq(Set.of()), anyInt()))
                .thenReturn(new Cell(0, 0));
        when(moveGenerationStrategy.nextMove(eq(Symbol.O), eq(Set.of(new Cell(0, 0))), anyInt()))
                .thenReturn(new Cell(1, 1));
        when(gameEngineClient.submitMove(any(), any(), anyInt(), anyInt()))
                .thenReturn(new EngineGameState(SessionStatus.IN_PROGRESS, null))
                .thenReturn(new EngineGameState(SessionStatus.DRAW, null));

        sessionService.simulate("s1");

        verify(moveGenerationStrategy).nextMove(eq(Symbol.O), eq(Set.of(new Cell(0, 0))), anyInt());
    }
}
