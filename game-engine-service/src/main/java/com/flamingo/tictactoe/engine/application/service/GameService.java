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
 * Handles the game engine's actions: create a game, make a move, read a
 * game's state. The actual game rules live in Game - this class just wires
 * that up to the repository. No HTTP/JSON code here either, that's in the
 * web controller.
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
        // Lock on this specific game so two moves for the same game can't
        // run at the same time and corrupt the board.
        synchronized (game) {
            game.applyMove(symbol, new Position(row, col));
            gameRepository.save(game);
            return game.toSnapshot();
        }
    }

    @Override
    public GameSnapshot getGame(String gameId) {
        Game game = requireGame(gameId);
        // Lock on the same game object as makeMove. Without this, a GET
        // request could read the board while a move is being written to it
        // (it's a plain array, not thread-safe) and see a half-updated or
        // stale board.
        synchronized (game) {
            return game.toSnapshot();
        }
    }

    private Game requireGame(String gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
    }
}
