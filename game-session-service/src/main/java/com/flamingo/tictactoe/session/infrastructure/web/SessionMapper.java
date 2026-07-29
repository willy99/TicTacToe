package com.flamingo.tictactoe.session.infrastructure.web;

import com.flamingo.tictactoe.session.domain.model.Move;
import com.flamingo.tictactoe.session.domain.model.SessionSnapshot;
import com.flamingo.tictactoe.session.infrastructure.web.dto.MoveDto;
import com.flamingo.tictactoe.session.infrastructure.web.dto.SessionResponse;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Translates domain snapshots into wire-level DTOs.
 */
@Component
public class SessionMapper {

    public SessionResponse toResponse(SessionSnapshot snapshot) {
        List<MoveDto> moves = snapshot.moves().stream()
                .map(this::toMoveDto)
                .toList();

        String winner = snapshot.winner() == null ? null : snapshot.winner().name();
        return new SessionResponse(
                snapshot.sessionId(),
                snapshot.boardSize(),
                snapshot.status().name(),
                winner,
                snapshot.failureReason(),
                moves);
    }

    private MoveDto toMoveDto(Move move) {
        return new MoveDto(move.moveNumber(), move.symbol().name(), move.row(), move.col());
    }
}
