package com.flamingo.tictactoe.session.infrastructure.web;

import com.flamingo.tictactoe.session.application.port.in.CreateSessionUseCase;
import com.flamingo.tictactoe.session.application.port.in.GetSessionUseCase;
import com.flamingo.tictactoe.session.application.port.in.SimulateGameUseCase;
import com.flamingo.tictactoe.session.domain.model.SessionSnapshot;
import com.flamingo.tictactoe.session.infrastructure.web.dto.SessionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The HTTP endpoints for game sessions. No simulation logic here - it
 * just turns requests into calls to the service above, and turns the
 * results back into JSON.
 */
@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final CreateSessionUseCase createSessionUseCase;
    private final SimulateGameUseCase simulateGameUseCase;
    private final GetSessionUseCase getSessionUseCase;
    private final SessionMapper sessionMapper;

    public SessionController(CreateSessionUseCase createSessionUseCase,
                              SimulateGameUseCase simulateGameUseCase,
                              GetSessionUseCase getSessionUseCase,
                              SessionMapper sessionMapper) {
        this.createSessionUseCase = createSessionUseCase;
        this.simulateGameUseCase = simulateGameUseCase;
        this.getSessionUseCase = getSessionUseCase;
        this.sessionMapper = sessionMapper;
    }

    @PostMapping
    public ResponseEntity<SessionResponse> createSession() {
        SessionSnapshot snapshot = createSessionUseCase.createSession();
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionMapper.toResponse(snapshot));
    }

    /**
     * Starts the automated simulation and returns right away - it doesn't
     * wait for the game to finish. Poll GET /sessions/{sessionId} to see
     * progress and the final result.
     */
    @PostMapping("/{sessionId}/simulate")
    public ResponseEntity<SessionResponse> simulate(@PathVariable("sessionId") String sessionId) {
        SessionSnapshot snapshot = simulateGameUseCase.simulate(sessionId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(sessionMapper.toResponse(snapshot));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionResponse> getSession(@PathVariable("sessionId") String sessionId) {
        SessionSnapshot snapshot = getSessionUseCase.getSession(sessionId);
        return ResponseEntity.ok(sessionMapper.toResponse(snapshot));
    }
}
