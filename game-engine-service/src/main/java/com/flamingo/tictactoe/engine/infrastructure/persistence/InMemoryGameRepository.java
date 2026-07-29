package com.flamingo.tictactoe.engine.infrastructure.persistence;

import com.flamingo.tictactoe.engine.application.port.out.GameRepository;
import com.flamingo.tictactoe.engine.domain.model.Game;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * Stores games in memory using a ConcurrentHashMap. Covers the assignment's
 * "in-memory data structure" requirement, and it's easy to swap for a real
 * database later since everything else only depends on GameRepository, not
 * on this class.
 */
@Repository
public class InMemoryGameRepository implements GameRepository {

    private final Map<String, Game> games = new ConcurrentHashMap<>();

    @Override
    public Optional<Game> findById(String gameId) {
        return Optional.ofNullable(games.get(gameId));
    }

    @Override
    public Game save(Game game) {
        games.put(game.id(), game);
        return game;
    }
}
