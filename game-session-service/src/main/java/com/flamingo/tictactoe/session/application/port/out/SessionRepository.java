package com.flamingo.tictactoe.session.application.port.out;

import com.flamingo.tictactoe.session.domain.model.Session;
import java.util.Optional;

/**
 * Outbound port for persisting and retrieving {@link Session} aggregates.
 */
public interface SessionRepository {

    Optional<Session> findById(String sessionId);

    Session save(Session session);
}
