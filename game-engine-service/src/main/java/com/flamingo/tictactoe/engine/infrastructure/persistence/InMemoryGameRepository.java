package com.flamingo.tictactoe.engine.infrastructure.persistence;

import com.flamingo.tictactoe.engine.application.port.out.GameRepository;
import com.flamingo.tictactoe.engine.domain.model.Game;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * In-memory {@link GameRepository} adapter backed by a {@link ConcurrentHashMap}.
 * Satisfies the assignment's "in-memory data structure" requirement while keeping
 * the storage technology fully swappable behind the port interface.
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
