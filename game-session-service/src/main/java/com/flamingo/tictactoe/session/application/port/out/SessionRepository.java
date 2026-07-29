package com.flamingo.tictactoe.session.application.port.out;

import com.flamingo.tictactoe.session.domain.model.Session;
import java.util.Optional;

/**
 * Where sessions get saved and loaded from.
 */
public interface SessionRepository {

    Optional<Session> findById(String sessionId);

    Session save(Session session);
}
