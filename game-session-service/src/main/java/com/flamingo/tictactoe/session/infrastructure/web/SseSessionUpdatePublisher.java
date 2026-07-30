package com.flamingo.tictactoe.session.infrastructure.web;

import com.flamingo.tictactoe.session.application.port.out.SessionUpdatePublisher;
import com.flamingo.tictactoe.session.domain.model.SessionSnapshot;
import com.flamingo.tictactoe.session.infrastructure.web.dto.SessionResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Keeps track of every open GET /sessions/{id}/stream connection, and sends
 * the latest session state to all of them whenever SessionService reports a
 * change. This is what lets the stream push updates instead of the UI
 * having to poll.
 */
@Component
public class SseSessionUpdatePublisher implements SessionUpdatePublisher {

    // Longer than any game should realistically take, so the connection
    // doesn't time out mid-game. It gets closed on its own once the game
    // reaches a final result anyway (see sendTo()).
    private static final long TIMEOUT_MILLIS = 5 * 60 * 1000;

    private final Map<String, List<SseEmitter>> emittersBySessionId = new ConcurrentHashMap<>();
    private final SessionMapper sessionMapper;

    public SseSessionUpdatePublisher(SessionMapper sessionMapper) {
        this.sessionMapper = sessionMapper;
    }

    /**
     * Opens a new stream for a session and immediately sends its current
     * state, so a client that connects mid-game sees where things stand
     * right away instead of waiting for the next move.
     */
    public SseEmitter subscribe(String sessionId, SessionSnapshot currentSnapshot) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MILLIS);
        List<SseEmitter> emitters = emittersBySessionId.computeIfAbsent(sessionId, id -> new CopyOnWriteArrayList<>());
        emitters.add(emitter);

        Runnable stopTracking = () -> emitters.remove(emitter);
        emitter.onCompletion(stopTracking);
        emitter.onTimeout(stopTracking);
        emitter.onError(ex -> stopTracking.run());

        sendTo(emitter, currentSnapshot);
        return emitter;
    }

    @Override
    public void publish(SessionSnapshot snapshot) {
        List<SseEmitter> emitters = emittersBySessionId.get(snapshot.sessionId());
        if (emitters == null) {
            return;
        }
        for (SseEmitter emitter : List.copyOf(emitters)) {
            sendTo(emitter, snapshot);
        }
    }

    private void sendTo(SseEmitter emitter, SessionSnapshot snapshot) {
        SessionResponse response = sessionMapper.toResponse(snapshot);
        try {
            emitter.send(response);
            if (!"IN_PROGRESS".equals(response.status())) {
                // The game is over - nothing more will ever be sent for
                // this session, so close the connection instead of leaving
                // it open and idle.
                emitter.complete();
            }
        } catch (IOException ex) {
            emitter.completeWithError(ex);
        }
    }
}
