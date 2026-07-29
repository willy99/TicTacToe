package com.flamingo.tictactoe.engine.application.service;

import com.flamingo.tictactoe.engine.application.port.in.CreateGameUseCase;
import com.flamingo.tictactoe.engine.application.port.in.GetGameUseCase;
import com.flamingo.tictactoe.engine.application.port.in.MakeMoveUseCase;
import com.flamingo.tictactoe.engine.application.port.out.GameRepository;
import com.flamingo.tictactoe.engine.domain.exception.GameNotFoundException;
import com.flamingo.tictactoe.engine.domain.model.Game;
import com.flamingo.tictactoe.engine.domain.model.GameSnapshot;
import com.flamingo.tictactoe.engine.domain.model.Position;
import com.flamingo.tictactoe.engine.domain.model.Symbol;
import org.springframework.stereotype.Service;

/**
 * Application service implementing every use case exposed by the Game Engine.
 * Orchestrates the domain model and the repository port; contains no board rules
 * itself (those live in {@link Game}) and no HTTP/JSON concerns (those live in
 * the web adapter).
 */
@Service
public class GameService implements CreateGameUseCase, MakeMoveUseCase, GetGameUseCase {

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Override
    public GameSnapshot initializeGame(String gameId, int boardSize) {
        Game game = gameRepository.findById(gameId)
                .orElseGet(() -> gameRepository.save(new Game(gameId, boardSize)));
        return game.toSnapshot();
    }

    @Override
    public GameSnapshot makeMove(String gameId, Symbol symbol, int row, int col) {
        Game game = requireGame(gameId);
        // Synchronize per-game-instance so two concurrent moves on the same game
        // cannot interleave and corrupt the board state.
        synchronized (game) {
            game.applyMove(symbol, new Position(row, col));
            gameRepository.save(game);
            return game.toSnapshot();
        }
    }

    @Override
    public GameSnapshot getGame(String gameId) {
        Game game = requireGame(gameId);
        // Same monitor as makeMove: without this, a GET racing a concurrent
        // move could read the board's backing array mid-write (it's a plain
        // Symbol[][], not a thread-safe structure) and observe a torn or
        // stale state.
        synchronized (game) {
            return game.toSnapshot();
        }
    }

    private Game requireGame(String gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
    }
}
