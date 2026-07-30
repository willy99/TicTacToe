package com.flamingo.tictactoe.session.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for SessionEntity - talks to H2 through JPA.
 * Separate from the SessionRepository port that the rest of the app
 * actually uses; this one is just the scaffold for a future persistent
 * adapter.
 */
public interface SessionJpaRepository extends JpaRepository<SessionEntity, String> {
}
