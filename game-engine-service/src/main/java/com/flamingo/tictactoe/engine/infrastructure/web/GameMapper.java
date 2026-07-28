package com.flamingo.tictactoe.engine.infrastructure.web;

import com.flamingo.tictactoe.engine.domain.model.GameSnapshot;
import com.flamingo.tictactoe.engine.domain.model.Symbol;
import com.flamingo.tictactoe.engine.infrastructure.web.dto.GameResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Translates domain snapshots into wire-level DTOs. Keeps {@link Symbol} and other
 * domain types from leaking into the HTTP contract.
 */
@Component
public class GameMapper {

    public GameResponse toResponse(GameSnapshot snapshot) {
        List<List<String>> board = new ArrayList<>(snapshot.cells().length);
        for (Symbol[] row : snapshot.cells()) {
            List<String> mappedRow = new ArrayList<>(row.length);
            for (Symbol cell : row) {
                mappedRow.add(cell == null ? null : cell.name());
            }
            board.add(mappedRow);
        }

        String winner = snapshot.winner() == null ? null : snapshot.winner().name();
        return new GameResponse(snapshot.gameId(), board, snapshot.status().name(), winner);
    }
}
