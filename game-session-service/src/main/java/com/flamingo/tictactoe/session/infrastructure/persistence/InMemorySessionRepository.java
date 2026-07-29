package com.flamingo.tictactoe.session.infrastructure.persistence;

import com.flamingo.tictactoe.session.application.port.out.SessionRepository;
import com.flamingo.tictactoe.session.domain.model.Session;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * Stores sessions in memory using a ConcurrentHashMap.
 */
@Repository
public class InMemorySessionRepository implements SessionRepository {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    @Override
    public Optional<Session> findById(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public Session save(Session session) {
        sessions.put(session.id(), session);
        return session;
    }
}
