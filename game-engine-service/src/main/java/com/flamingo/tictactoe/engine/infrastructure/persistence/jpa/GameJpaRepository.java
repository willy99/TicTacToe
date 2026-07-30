package com.flamingo.tictactoe.engine.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for GameEntity - talks to H2 through JPA. Separate
 * from the GameRepository port that the rest of the app actually uses; this
 * one is just the scaffold for a future persistent adapter.
 */
public interface GameJpaRepository extends JpaRepository<GameEntity, String> {
}
