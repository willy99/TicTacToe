package com.flamingo.tictactoe.engine.application.port.out;

import com.flamingo.tictactoe.engine.domain.model.Game;
import java.util.Optional;

/**
 * Where games get saved and loaded from. The rest of the code only depends
 * on this interface, not on how games are actually stored - today that's
 * an in-memory map, but it could be swapped for a real database later
 * without changing anything else.
 */
public interface GameRepository {

    Optional<Game> findById(String gameId);

    Game save(Game game);
}
