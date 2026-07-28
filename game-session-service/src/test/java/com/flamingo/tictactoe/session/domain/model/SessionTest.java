package com.flamingo.tictactoe.session.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flamingo.tictactoe.session.domain.exception.SessionAlreadyCompletedException;
import org.junit.jupiter.api.Test;

class SessionTest {

    @Test
    void startsInProgressWithNoMoves() {
        Session session = new Session("s1");

        assertThat(session.isInProgress()).isTrue();
        assertThat(session.occupiedCells()).isEmpty();
        assertThat(session.toSnapshot().moves()).isEmpty();
    }

    @Test
    void xAlwaysOpensAndPlayersAlternate() {
        Session session = new Session("s1");

        assertThat(session.nextSymbol()).isEqualTo(Symbol.X);

        session.recordMove(Symbol.X, 0, 0, SessionStatus.IN_PROGRESS, null);
        assertThat(session.nextSymbol()).isEqualTo(Symbol.O);

        session.recordMove(Symbol.O, 1, 1, SessionStatus.IN_PROGRESS, null);
        assertThat(session.nextSymbol()).isEqualTo(Symbol.X);
    }

    @Test
    void tracksOccupiedCellsFromRecordedMoves() {
        Session session = new Session("s1");
        session.recordMove(Symbol.X, 0, 0, SessionStatus.IN_PROGRESS, null);
        session.recordMove(Symbol.O, 1, 1, SessionStatus.IN_PROGRESS, null);

        assertThat(session.occupiedCells()).containsExactlyInAnyOrder(new Cell(0, 0), new Cell(1, 1));
    }

    @Test
    void recordingAWinUpdatesStatusAndWinner() {
        Session session = new Session("s1");

        session.recordMove(Symbol.X, 0, 0, SessionStatus.WIN, Symbol.X);

        assertThat(session.isInProgress()).isFalse();
        assertThat(session.toSnapshot().status()).isEqualTo(SessionStatus.WIN);
        assertThat(session.toSnapshot().winner()).isEqualTo(Symbol.X);
    }

    @Test
    void rejectsRecordingAMoveOnceTheSessionHasCompleted() {
        Session session = new Session("s1");
        session.recordMove(Symbol.X, 0, 0, SessionStatus.WIN, Symbol.X);

        assertThatThrownBy(() -> session.recordMove(Symbol.O, 1, 1, SessionStatus.IN_PROGRESS, null))
                .isInstanceOf(SessionAlreadyCompletedException.class);
    }

    @Test
    void snapshotMoveHistoryPreservesPlayOrder() {
        Session session = new Session("s1");
        session.recordMove(Symbol.X, 0, 0, SessionStatus.IN_PROGRESS, null);
        session.recordMove(Symbol.O, 1, 1, SessionStatus.IN_PROGRESS, null);

        var moves = session.toSnapshot().moves();
        assertThat(moves).hasSize(2);
        assertThat(moves.get(0).moveNumber()).isEqualTo(1);
        assertThat(moves.get(1).moveNumber()).isEqualTo(2);
    }
}
