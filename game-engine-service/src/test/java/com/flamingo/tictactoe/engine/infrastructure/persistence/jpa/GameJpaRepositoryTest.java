package com.flamingo.tictactoe.engine.infrastructure.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Proves the JPA scaffold actually works against a real H2 database - not
 * that it's wired into the app (it isn't), just that this direction is
 * real and not dead code that would fail the moment someone tried it.
 */
@DataJpaTest
class GameJpaRepositoryTest {

    @Autowired
    private GameJpaRepository repository;

    @Test
    void savesAndReloadsAGame() {
        repository.save(new GameEntity("g1", 3, "IN_PROGRESS", null));

        GameEntity found = repository.findById("g1").orElseThrow();

        assertThat(found.getBoardSize()).isEqualTo(3);
        assertThat(found.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(found.getWinner()).isNull();
    }
}
