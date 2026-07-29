package com.flamingo.tictactoe.engine.infrastructure.web;

import com.flamingo.tictactoe.engine.application.port.in.CreateGameUseCase;
import com.flamingo.tictactoe.engine.application.port.in.GetGameUseCase;
import com.flamingo.tictactoe.engine.application.port.in.MakeMoveUseCase;
import com.flamingo.tictactoe.engine.domain.model.GameSnapshot;
import com.flamingo.tictactoe.engine.domain.model.Symbol;
import com.flamingo.tictactoe.engine.infrastructure.config.BoardProperties;
import com.flamingo.tictactoe.engine.infrastructure.web.dto.GameResponse;
import com.flamingo.tictactoe.engine.infrastructure.web.dto.MoveRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The HTTP endpoints for the game engine. No game rules here - it just
 * turns requests into calls to the service above, and turns the results
 * back into JSON.
 */
@RestController
@RequestMapping("/games")
public class GameController {

    private final CreateGameUseCase createGameUseCase;
    private final MakeMoveUseCase makeMoveUseCase;
    private final GetGameUseCase getGameUseCase;
    private final GameMapper gameMapper;
    private final BoardProperties boardProperties;

    public GameController(CreateGameUseCase createGameUseCase,
                           MakeMoveUseCase makeMoveUseCase,
                           GetGameUseCase getGameUseCase,
                           GameMapper gameMapper,
                           BoardProperties boardProperties) {
        this.createGameUseCase = createGameUseCase;
        this.makeMoveUseCase = makeMoveUseCase;
        this.getGameUseCase = getGameUseCase;
        this.gameMapper = gameMapper;
        this.boardProperties = boardProperties;
    }

    /**
     * Creates a new game with the given id, or just returns the existing
     * one if it's already there (safe to call twice). boardSize is
     * optional - if it's left out, the configured default
     * (game-engine.board.default-size) is used instead.
     */
    @PutMapping("/{gameId}")
    public ResponseEntity<GameResponse> initializeGame(
            @PathVariable("gameId") String gameId,
            @RequestParam(name = "boardSize", required = false) Integer boardSize) {
        int resolvedBoardSize = boardSize != null ? boardSize : boardProperties.getDefaultSize();
        GameSnapshot snapshot = createGameUseCase.initializeGame(gameId, resolvedBoardSize);
        return ResponseEntity.status(HttpStatus.OK).body(gameMapper.toResponse(snapshot));
    }

    /**
     * Tries to make a move on the board with a certain position (row, col) and Symbol
     */
    @PostMapping("/{gameId}/move")
    public ResponseEntity<GameResponse> makeMove(@PathVariable("gameId") String gameId,
                                                  @Valid @RequestBody MoveRequest request) {
        GameSnapshot snapshot = makeMoveUseCase.makeMove(
                gameId, Symbol.valueOf(request.symbol()), request.row(), request.col());
        return ResponseEntity.ok(gameMapper.toResponse(snapshot));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameResponse> getGame(@PathVariable("gameId") String gameId) {
        GameSnapshot snapshot = getGameUseCase.getGame(gameId);
        return ResponseEntity.ok(gameMapper.toResponse(snapshot));
    }
}
