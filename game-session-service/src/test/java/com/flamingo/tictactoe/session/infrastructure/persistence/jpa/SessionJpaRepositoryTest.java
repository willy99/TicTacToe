package com.flamingo.tictactoe.session.infrastructure.persistence.jpa;

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
class SessionJpaRepositoryTest {

    @Autowired
    private SessionJpaRepository repository;

    @Test
    void savesAndReloadsASession() {
        repository.save(new SessionEntity("s1", 3, "WIN", "X", null));

        SessionEntity found = repository.findById("s1").orElseThrow();

        assertThat(found.getBoardSize()).isEqualTo(3);
        assertThat(found.getStatus()).isEqualTo("WIN");
        assertThat(found.getWinner()).isEqualTo("X");
    }
}
