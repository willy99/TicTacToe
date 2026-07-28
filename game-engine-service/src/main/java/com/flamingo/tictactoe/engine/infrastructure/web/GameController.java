package com.flamingo.tictactoe.engine.infrastructure.web;

import com.flamingo.tictactoe.engine.application.port.in.CreateGameUseCase;
import com.flamingo.tictactoe.engine.application.port.in.GetGameUseCase;
import com.flamingo.tictactoe.engine.application.port.in.MakeMoveUseCase;
import com.flamingo.tictactoe.engine.domain.model.GameSnapshot;
import com.flamingo.tictactoe.engine.domain.model.Symbol;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter exposing the Game Engine's use cases over HTTP. Contains no game
 * rules; it only translates HTTP requests into use case calls and domain
 * snapshots into DTOs.
 */
@RestController
@RequestMapping("/games")
public class GameController {

    private final CreateGameUseCase createGameUseCase;
    private final MakeMoveUseCase makeMoveUseCase;
    private final GetGameUseCase getGameUseCase;
    private final GameMapper gameMapper;

    public GameController(CreateGameUseCase createGameUseCase,
                           MakeMoveUseCase makeMoveUseCase,
                           GetGameUseCase getGameUseCase,
                           GameMapper gameMapper) {
        this.createGameUseCase = createGameUseCase;
        this.makeMoveUseCase = makeMoveUseCase;
        this.getGameUseCase = getGameUseCase;
        this.gameMapper = gameMapper;
    }

    /**
     * Initializes a new game under the given id, or returns the existing one
     * unchanged if it was already initialized. Idempotent by design so callers
     * (e.g. the Game Session Service) can safely retry.
     */
    @PutMapping("/{gameId}")
    public ResponseEntity<GameResponse> initializeGame(@PathVariable("gameId") String gameId) {
        GameSnapshot snapshot = createGameUseCase.initializeGame(gameId);
        return ResponseEntity.status(HttpStatus.OK).body(gameMapper.toResponse(snapshot));
    }

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
