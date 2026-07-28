package com.flamingo.tictactoe.engine.application.port.out;

import com.flamingo.tictactoe.engine.domain.model.Game;
import java.util.Optional;

/**
 * Outbound port for persisting and retrieving {@link Game} aggregates. The application
 * layer depends only on this abstraction; the concrete storage technology (in-memory
 * map today, a database tomorrow) is plugged in by the infrastructure layer.
 */
public interface GameRepository {

    Optional<Game> findById(String gameId);

    Game save(Game game);
}
