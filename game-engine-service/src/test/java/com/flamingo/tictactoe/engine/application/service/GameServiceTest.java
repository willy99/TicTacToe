package com.flamingo.tictactoe.engine.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flamingo.tictactoe.engine.application.port.out.GameRepository;
import com.flamingo.tictactoe.engine.domain.exception.GameNotFoundException;
import com.flamingo.tictactoe.engine.domain.model.Game;
import com.flamingo.tictactoe.engine.domain.model.GameSnapshot;
import com.flamingo.tictactoe.engine.domain.model.GameStatus;
import com.flamingo.tictactoe.engine.domain.model.Symbol;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameService = new GameService(gameRepository);
    }

    @Test
    void initializeGameCreatesANewGameWhenNoneExists() {
        when(gameRepository.findById("g1")).thenReturn(Optional.empty());
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameSnapshot snapshot = gameService.initializeGame("g1", 3);

        assertThat(snapshot.gameId()).isEqualTo("g1");
        assertThat(snapshot.status()).isEqualTo(GameStatus.IN_PROGRESS);
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    void initializeGameHonorsARequestedBoardSize() {
        when(gameRepository.findById("g1")).thenReturn(Optional.empty());
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameSnapshot snapshot = gameService.initializeGame("g1", 5);

        assertThat(snapshot.cells()).hasDimensions(5, 5);
    }

    @Test
    void initializeGameIsIdempotentForAnExistingGame() {
        Game existing = new Game("g1");
        when(gameRepository.findById("g1")).thenReturn(Optional.of(existing));

        GameSnapshot snapshot = gameService.initializeGame("g1", 3);

        assertThat(snapshot.gameId()).isEqualTo("g1");
        verify(gameRepository, never()).save(any());
    }

    @Test
    void makeMoveDelegatesToTheGameAndPersistsTheResult() {
        Game existing = new Game("g1");
        when(gameRepository.findById("g1")).thenReturn(Optional.of(existing));

        GameSnapshot snapshot = gameService.makeMove("g1", Symbol.X, 0, 0);

        assertThat(snapshot.cells()[0][0]).isEqualTo(Symbol.X);
        verify(gameRepository).save(existing);
    }

    @Test
    void makeMoveThrowsWhenGameDoesNotExist() {
        when(gameRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.makeMove("missing", Symbol.X, 0, 0))
                .isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void getGameThrowsWhenGameDoesNotExist() {
        when(gameRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.getGame("missing"))
                .isInstanceOf(GameNotFoundException.class);
    }
}
